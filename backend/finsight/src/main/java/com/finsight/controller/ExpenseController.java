package com.finsight.controller;

import com.finsight.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/breakdown")
    public ResponseEntity<Map<String, BigDecimal>> getExpenseBreakdown(
            @RequestParam Map<String, BigDecimal> expenses) {
        return ResponseEntity.ok(expenseService.calculateExpenseBreakdown(expenses));
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses(
            @RequestParam Map<String, BigDecimal> expenses) {
        return ResponseEntity.ok(expenseService.calculateTotalExpenses(expenses));
    }

    @GetMapping("/fixed-variable-split")
    public ResponseEntity<Map<String, BigDecimal>> getFixedVariableSplit(
            @RequestParam BigDecimal fixedCosts,
            @RequestParam BigDecimal variableCosts) {
        return ResponseEntity.ok(expenseService.calculateFixedVariableSplit(fixedCosts, variableCosts));
    }
}
