package com.finsight.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class ExpenseService {

    public BigDecimal calculateTotalExpenses(Map<String, BigDecimal> expenses) {
        return expenses.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> calculateExpenseBreakdown(Map<String, BigDecimal> expenses) {
        BigDecimal total = calculateTotalExpenses(expenses);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        expenses.forEach((category, amount) -> {
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : amount.divide(total, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
            breakdown.put(category, percentage);
        });
        return breakdown;
    }

    public BigDecimal calculateFixedCosts(BigDecimal salaries, BigDecimal rent) {
        return salaries.add(rent);
    }

    public BigDecimal calculateVariableCosts(BigDecimal totalExpenses, BigDecimal fixedCosts) {
        return totalExpenses.subtract(fixedCosts);
    }

    public Map<String, BigDecimal> getExpenseSummary(Map<String, BigDecimal> expenses, BigDecimal salaries, BigDecimal rent) {
        BigDecimal totalExpenses = calculateTotalExpenses(expenses);
        BigDecimal fixedCosts = calculateFixedCosts(salaries, rent);
        BigDecimal variableCosts = calculateVariableCosts(totalExpenses, fixedCosts);

        BigDecimal fixedPercentage = totalExpenses.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : fixedCosts.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

        BigDecimal variablePercentage = totalExpenses.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : variableCosts.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalExpenses", totalExpenses);
        summary.put("fixedCosts", fixedCosts);
        summary.put("variableCosts", variableCosts);
        summary.put("fixedPercentage", fixedPercentage);
        summary.put("variablePercentage", variablePercentage);
        return summary;
    }

    public Map<String, Object> getFullExpenseReport(Map<String, BigDecimal> expenses, BigDecimal salaries, BigDecimal rent) {
        Map<String, Object> report = new HashMap<>();
        report.put("breakdown", calculateExpenseBreakdown(expenses));
        report.put("summary", getExpenseSummary(expenses, salaries, rent));

        BigDecimal total = calculateTotalExpenses(expenses);
        String largestCategory = "";
        BigDecimal largestAmount = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : expenses.entrySet()) {
            if (entry.getValue().compareTo(largestAmount) > 0) {
                largestAmount = entry.getValue();
                largestCategory = entry.getKey();
            }
        }
        report.put("largestExpenseCategory", largestCategory);
        report.put("largestExpenseAmount", largestAmount);
        return report;
    }
}
