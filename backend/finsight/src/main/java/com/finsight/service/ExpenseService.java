package com.finsight.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    public double calculateTotalExpenses(List<Double> expenses) {
        return expenses.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public double calculateExpenseToRevenueRatio(double totalExpenses, double totalRevenue) {
        if (totalRevenue == 0) return 0;
        return (totalExpenses / totalRevenue) * 100;
    }

    public double calculateExpenseGrowthRate(double currentMonth, double previousMonth) {
        if (previousMonth == 0) return 0;
        return ((currentMonth - previousMonth) / previousMonth) * 100;
    }

    public Map<String, Double> calculateFixedVsVariable(double fixedCosts, double variableCosts) {
        double total = fixedCosts + variableCosts;
        return Map.of(
            "fixedCosts", fixedCosts,
            "variableCosts", variableCosts,
            "fixedPercentage", (fixedCosts / total) * 100,
            "variablePercentage", (variableCosts / total) * 100
        );
    }

    public Map.Entry<String, Double> findLargestExpenseCategory(Map<String, Double> expensesByCategory) {
        return expensesByCategory.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    public Map<String, Double> calculateCategoryPercentages(Map<String, Double> expensesByCategory) {
        double total = expensesByCategory.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return expensesByCategory.entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> (e.getValue() / total) * 100
                ));
    }
}

