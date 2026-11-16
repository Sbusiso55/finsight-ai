package com.finsight.controller;

import com.finsight.dto.TransactionDTO;
import com.finsight.dto.TransactionRequest;
import com.finsight.model.Business;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.security.BusinessAccess;
import com.finsight.service.CategoryService;
import com.finsight.service.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final BusinessAccess businessAccess;

    public TransactionController(TransactionService transactionService,
                                 CategoryService categoryService,
                                 BusinessAccess businessAccess) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.businessAccess = businessAccess;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionDTO>> getTransactions(@RequestParam Long businessId, Pageable pageable,
                                                                @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(transactionService.findByBusinessId(businessId, pageable).map(TransactionDTO::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id, @RequestParam Long businessId,
                                                         @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return transactionService.findByIdAndBusinessId(id, businessId)
                .map(TransactionDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionRequest request,
                                                            @AuthenticationPrincipal UserDetails user) {
        Business business = businessAccess.requireOwned(request.getBusinessId(), user.getUsername());
        Transaction transaction = new Transaction();
        transaction.setBusiness(business);
        apply(transaction, request, business);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionDTO.from(transactionService.save(transaction)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable Long id, @RequestParam Long businessId,
                                                            @RequestBody TransactionRequest request,
                                                            @AuthenticationPrincipal UserDetails user) {
        Business business = businessAccess.requireOwned(businessId, user.getUsername());
        Transaction existing = transactionService.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        apply(existing, request, business);
        return ResponseEntity.ok(TransactionDTO.from(transactionService.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, @RequestParam Long businessId,
                                                  @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        transactionService.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(@RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(transactionService.getTotalRevenue(businessId, from, to));
    }

    @GetMapping("/expenses")
    public ResponseEntity<BigDecimal> getTotalExpenses(@RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(transactionService.getTotalExpenses(businessId, from, to));
    }

    private void apply(Transaction t, TransactionRequest r, Business business) {
        if (r.getAmount() == null || r.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }
        if (r.getType() == null || r.getTransactionDate() == null || r.getCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type, date and category are required");
        }
        Category category = categoryService.getCategoryById(r.getCategoryId())
                .filter(c -> c.getBusiness().getId().equals(business.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category"));
        t.setCategory(category);
        t.setAmount(r.getAmount());
        t.setType(r.getType());
        t.setTransactionDate(r.getTransactionDate());
        t.setDescription(r.getDescription());
    }
}