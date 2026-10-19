package com.finsight.controller;

import com.finsight.service.CashFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cashflow")
public class CashFlowController {

    private final CashFlowService cashFlowService;

    public CashFlowController(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    @GetMapping("/net")
    public ResponseEntity<BigDecimal> getNetCashFlow(
            @RequestParam BigDecimal totalInflows,
            @RequestParam BigDecimal totalOutflows) {
        return ResponseEntity.ok(cashFlowService.calculateNetCashFlow(totalInflows, totalOutflows));
    }

    @GetMapping("/closing-balance")
    public ResponseEntity<BigDecimal> getClosingBalance(
            @RequestParam BigDecimal openingBalance,
            @RequestParam BigDecimal netCashFlow) {
        return ResponseEntity.ok(cashFlowService.calculateClosingBalance(openingBalance, netCashFlow));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getCashFlowSummary(
            @RequestParam BigDecimal openingBalance,
            @RequestParam BigDecimal totalInflows,
            @RequestParam BigDecimal totalOutflows) {
        return ResponseEntity.ok(cashFlowService.getCashFlowSummary(openingBalance, totalInflows, totalOutflows));
    }
}
