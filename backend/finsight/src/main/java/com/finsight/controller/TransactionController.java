package com.finsight.controller;

import com.finsight.model.Transaction;
import com.finsight.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.save(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        return transactionService.findById(id)
                .map(existing -> {
                    transaction.setId(existing.getId());
                    return ResponseEntity.ok(transactionService.save(transaction));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam Long businessId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(transactionService.calculateTotalRevenue(
                businessId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    @GetMapping("/expenses")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @RequestParam Long businessId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(transactionService.calculateTotalExpenses(
                businessId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }
}
