package com.finsight.controller;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
import com.finsight.repository.TransactionRepository;
import com.finsight.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/business/{businessId}")
    public Page<Transaction> getTransactions(
            @PathVariable Long businessId,
            Pageable pageable) {
        return transactionService.getTransactionsByBusiness(businessId, pageable);
    }

    @GetMapping("/business/{businessId}/range")
    public Page<Transaction> getTransactionsByDateRange(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return transactionService.getTransactionsByDateRange(businessId, from, to, pageable);
    }

    @GetMapping("/business/{businessId}/list")
    public List<Transaction> getTransactionsList(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return transactionService.getTransactionsListByDateRange(businessId, from, to);
    }

    @GetMapping("/{id}/business/{businessId}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable Long id,
            @PathVariable Long businessId) {
        return ResponseEntity.ok(transactionService.getTransactionById(id, businessId));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction) {
        transaction.setId(id);
        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/business/{businessId}/total")
    public ResponseEntity<BigDecimal> getTotalByType(
            @PathVariable Long businessId,
            @RequestParam TransactionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTotalByType(businessId, type, from, to));
    }

    @GetMapping("/business/{businessId}/total-by-category")
    public ResponseEntity<BigDecimal> getTotalByCategory(
            @PathVariable Long businessId,
            @RequestParam Long categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTotalByCategory(businessId, categoryId, from, to));
    }

    @GetMapping("/business/{businessId}/totals-by-category")
    public ResponseEntity<List<TransactionRepository.CategoryTotal>> getTotalsByCategory(
            @PathVariable Long businessId,
            @RequestParam TransactionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transactionService.getTotalsByCategory(businessId, type, from, to));
    }
}
