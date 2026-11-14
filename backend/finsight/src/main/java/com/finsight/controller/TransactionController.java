package com.finsight.controller;

import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.service.CategoryService;
import com.finsight.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam Long businessId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByBusinessId(businessId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable Long id, @RequestParam Long businessId) {
        return transactionService.findByIdAndBusinessId(id, businessId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestParam Long businessId,
            @RequestBody Transaction transaction) {

        Optional<Transaction> existingTransaction =
                transactionService.findByIdAndBusinessId(id, businessId);

        if (existingTransaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Validate required transaction fields.
        if (transaction.getAmount() == null ||
                transaction.getAmount().signum() <= 0 ||
                transaction.getType() == null ||
                transaction.getTransactionDate() == null ||
                transaction.getCategory() == null ||
                transaction.getCategory().getId() == null) {

            return ResponseEntity.badRequest().build();
        }

        // Retrieve a category belonging to the same business.
        Optional<Category> category =
                categoryService.getCategoryByIdAndBusinessId(
                        transaction.getCategory().getId(),
                        businessId
                );

        if (category.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Ensure category type matches transaction type.
        if (category.get().getType() != transaction.getType()) {
            return ResponseEntity.badRequest().build();
        }

        Transaction existing = existingTransaction.get();

        // Update only the editable fields.
        existing.setDescription(transaction.getDescription());
        existing.setAmount(transaction.getAmount());
        existing.setType(transaction.getType());
        existing.setTransactionDate(transaction.getTransactionDate());
        existing.setCategory(category.get());

        return ResponseEntity.ok(
                transactionService.save(existing)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTotalRevenue(businessId, from, to));
    }

    @GetMapping("/expenses")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTotalExpenses(businessId, from, to));
    }
}


