package com.finsight.controller;

import com.finsight.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses(@RequestParam Map<String, BigDecimal> expenseBreakdown) {
        return ResponseEntity.ok(expenseService.calculateTotalExpenses(expenseBreakdown));
    }

    @GetMapping("/fixed-variable")
    public ResponseEntity<Map<String, BigDecimal>> getFixedVariableSplit(
            @RequestParam BigDecimal totalFixed,
            @RequestParam BigDecimal totalVariable) {
        return ResponseEntity.ok(expenseService.calculateFixedVsVariable(totalFixed, totalVariable));
    }

    @GetMapping("/ratio")
    public ResponseEntity<BigDecimal> getExpenseToRevenueRatio(
            @RequestParam BigDecimal totalExpenses,
            @RequestParam BigDecimal totalRevenue) {
        return ResponseEntity.ok(expenseService.calculateExpenseToRevenueRatio(totalExpenses, totalRevenue));
    }

    @GetMapping("/breakdown")
    public ResponseEntity<Map<String, Map<String, BigDecimal>>> getExpenseBreakdown(
            @RequestParam Map<String, BigDecimal> expenseBreakdown) {
        return ResponseEntity.ok(expenseService.getExpenseBreakdownWithPercentages(expenseBreakdown));
    }
}
