package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Page<Transaction> getTransactionsByBusiness(Long businessId, Pageable pageable) {
        return transactionRepository.findByBusinessId(businessId, pageable);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public BigDecimal getTotalRevenue(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> income = transactionRepository.findByBusinessIdAndTypeBetweenDates(
                businessId, TransactionType.INCOME, startDate, endDate);
        return income.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpenses(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndTypeBetweenDates(
                businessId, TransactionType.EXPENSE, startDate, endDate);
        return expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
