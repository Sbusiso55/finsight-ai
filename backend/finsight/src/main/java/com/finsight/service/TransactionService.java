package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    public BigDecimal calculateTotalRevenue(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalExpenses(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Transaction> findByBusinessIdAndDateBetween(Long businessId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);
    }
}
