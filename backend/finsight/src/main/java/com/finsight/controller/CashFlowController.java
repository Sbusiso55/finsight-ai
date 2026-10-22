
package com.finsight.controller;

import com.finsight.service.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cashflow")
public class CashFlowController {

    @Autowired
    private CashFlowService cashFlowService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getCashFlowSummary(
            @RequestParam BigDecimal openingBalance,
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(cashFlowService.calculateCashFlow(openingBalance, totalRevenue, totalExpenses));
    }
}
