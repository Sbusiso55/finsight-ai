package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getAllTransactionsByBusiness(Long businessId) {
        return transactionRepository.findByBusinessId(businessId);
    }

    public List<Transaction> getTransactionsByDateRange(Long businessId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByBusinessIdAndTransactionDateBetween(businessId, startDate, endDate);
    }

    public BigDecimal getTotalRevenue(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = getTransactionsByDateRange(businessId, startDate, endDate);
        return transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpenses(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = getTransactionsByDateRange(businessId, startDate, endDate);
        return transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
                .map(existing -> {
                    existing.setAmount(updatedTransaction.getAmount());
                    existing.setDescription(updatedTransaction.getDescription());
                    existing.setType(updatedTransaction.getType());
                    existing.setTransactionDate(updatedTransaction.getTransactionDate());
                    return transactionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
