package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, BigDecimal> getExpenseBreakdownWithPercentages(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .toList();

        BigDecimal total = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("totalExpenses", total);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, BigDecimal> categoryTotals = new HashMap<>();
            for (Transaction t : expenses) {
                String category = t.getCategory() != null ? t.getCategory().getName() : "Uncategorized";
                categoryTotals.merge(category, t.getAmount(), BigDecimal::add);
            }
            for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
                breakdown.put(entry.getKey(), entry.getValue());
                BigDecimal percentage = entry.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP);
                breakdown.put(entry.getKey() + "_percentage", percentage);
            }
        }
        return breakdown;
    }

    public Map<String, BigDecimal> calculateFixedVsVariable(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .toList();

        BigDecimal fixedCosts = BigDecimal.ZERO;
        BigDecimal variableCosts = BigDecimal.ZERO;

        for (Transaction t : expenses) {
            if (t.getCategory() != null && t.getCategory().getName() != null &&
                    (t.getCategory().getName().equalsIgnoreCase("Salaries") ||
                     t.getCategory().getName().equalsIgnoreCase("Rent"))) {
                fixedCosts = fixedCosts.add(t.getAmount());
            } else {
                variableCosts = variableCosts.add(t.getAmount());
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("fixedCosts", fixedCosts);
        result.put("variableCosts", variableCosts);
        return result;
    }

    public BigDecimal calculateExpenseToRevenueRatio(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalExpenses.multiply(new BigDecimal("100"))
                .divide(totalRevenue, 2, RoundingMode.HALF_UP);
    }
}
