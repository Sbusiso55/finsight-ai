package com.finsight.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    public BigDecimal calculateTotalExpenses(List<BigDecimal> expenses) {
        return expenses.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateExpenseToRevenueRatio(BigDecimal totalExpenses, BigDecimal totalRevenue) {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return totalExpenses.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public BigDecimal calculateExpenseGrowthRate(BigDecimal currentMonth, BigDecimal previousMonth) {
        if (previousMonth.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return currentMonth.subtract(previousMonth)
                .divide(previousMonth, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public Map<String, BigDecimal> calculateFixedVsVariable(BigDecimal fixedCosts, BigDecimal variableCosts) {
        BigDecimal total = fixedCosts.add(variableCosts);
        return Map.of(
            "fixedCosts", fixedCosts,
            "variableCosts", variableCosts,
            "fixedPercentage", fixedCosts.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")),
            "variablePercentage", variableCosts.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
        );
    }

    public Map.Entry<String, BigDecimal> findLargestExpenseCategory(Map<String, BigDecimal> expensesByCategory) {
        return expensesByCategory.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    public Map<String, BigDecimal> calculateCategoryPercentages(Map<String, BigDecimal> expensesByCategory) {
        BigDecimal total = expensesByCategory.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return expensesByCategory.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                ));
    }
}
