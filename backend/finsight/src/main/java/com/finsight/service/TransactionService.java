package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<Transaction> getTransactionsByBusiness(Long businessId, Pageable pageable) {
        return transactionRepository.findByBusinessId(businessId, pageable);
    }

    public Page<Transaction> getTransactionsByDateRange(Long businessId, LocalDate from, LocalDate to, Pageable pageable) {
        return transactionRepository.findByBusinessIdAndTransactionDateBetween(businessId, from, to, pageable);
    }

    public List<Transaction> getTransactionsListByDateRange(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.findByBusinessIdAndTransactionDateBetweenOrderByTransactionDateAsc(businessId, from, to);
    }

    public Transaction getTransactionById(Long id, Long businessId) {
        return transactionRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public BigDecimal getTotalByType(Long businessId, TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.sumByType(businessId, type, from, to);
    }

    public BigDecimal getTotalByCategory(Long businessId, Long categoryId, LocalDate from, LocalDate to) {
        return transactionRepository.sumByCategory(businessId, categoryId, from, to);
    }

    public List<TransactionRepository.CategoryTotal> getTotalsByCategory(Long businessId, TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.totalsByCategory(businessId, type, from, to);
    }
}
