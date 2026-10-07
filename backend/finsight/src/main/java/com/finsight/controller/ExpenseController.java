package com.finsight.controller;

import com.finsight.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping("/total")
    public double getTotalExpenses(@RequestBody List<Double> expenses) {
        return expenseService.calculateTotalExpenses(expenses);
    }

    @GetMapping("/ratio")
    public double getExpenseToRevenueRatio(
            @RequestParam double totalExpenses,
            @RequestParam double totalRevenue) {
        return expenseService.calculateExpenseToRevenueRatio(totalExpenses, totalRevenue);
    }

    @GetMapping("/growth-rate")
    public double getExpenseGrowthRate(
            @RequestParam double currentMonth,
            @RequestParam double previousMonth) {
        return expenseService.calculateExpenseGrowthRate(currentMonth, previousMonth);
    }

    @PostMapping("/category-breakdown")
    public Map<String, Double> getCategoryBreakdown(
            @RequestBody Map<String, Double> expensesByCategory) {
        return expenseService.calculateCategoryPercentages(expensesByCategory);
    }

    @PostMapping("/largest-category")
    public Map.Entry<String, Double> getLargestCategory(
            @RequestBody Map<String, Double> expensesByCategory) {
        return expenseService.findLargestExpenseCategory(expensesByCategory);
    }

    @GetMapping("/fixed-vs-variable")
    public Map<String, Double> getFixedVsVariable(
            @RequestParam double fixedCosts,
            @RequestParam double variableCosts) {
        return expenseService.calculateFixedVsVariable(fixedCosts, variableCosts);
    }
}
