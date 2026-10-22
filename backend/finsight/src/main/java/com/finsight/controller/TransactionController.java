
package com.finsight.controller;

import com.finsight.model.Transaction;
import com.finsight.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
            @PathVariable Long id, @RequestParam Long businessId,
            @RequestBody Transaction transaction) {
        return transactionService.findByIdAndBusinessId(id, businessId)
                .map(existing -> {
                    transaction.setId(id);
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

