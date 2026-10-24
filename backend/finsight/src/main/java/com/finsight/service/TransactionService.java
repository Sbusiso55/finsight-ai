
package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.enums.CostClassification;
import com.finsight.model.enums.TransactionType;
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

    public Page<Transaction> findByBusinessId(Long businessId, Pageable pageable) {
        return transactionRepository.findByBusinessId(businessId, pageable);
    }

    public Page<Transaction> findByBusinessIdAndDateRange(Long businessId, LocalDate from, LocalDate to, Pageable pageable) {
        return transactionRepository.findByBusinessIdAndTransactionDateBetween(businessId, from, to, pageable);
    }

    public List<Transaction> findByBusinessIdAndDateRangeList(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.findByBusinessIdAndTransactionDateBetweenOrderByTransactionDateAsc(businessId, from, to);
    }

    public Optional<Transaction> findByIdAndBusinessId(Long id, Long businessId) {
        return transactionRepository.findByIdAndBusinessId(id, businessId);
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    public BigDecimal getTotalRevenue(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.sumByType(businessId, TransactionType.INCOME, from, to);
    }

    public BigDecimal getTotalExpenses(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.sumByType(businessId, TransactionType.EXPENSE, from, to);
    }

    public List<TransactionRepository.CategoryTotal> getRevenueByCategory(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.totalsByCategory(businessId, TransactionType.INCOME, from, to);
    }

    public List<TransactionRepository.CategoryTotal> getExpensesByCategory(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.totalsByCategory(businessId, TransactionType.EXPENSE, from, to);
    }


    public BigDecimal getCostOfGoodsSold(
            Long businessId, LocalDate from, LocalDate to) {

        return transactionRepository.sumByCostClassification(
                businessId,
                CostClassification.COST_OF_GOODS_SOLD,
                from,
                to);
    }


}

