package com.finsight.controller;

import com.finsight.service.ProfitMarginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/profit")
public class ProfitMarginController {

    private final ProfitMarginService profitMarginService;

    public ProfitMarginController(ProfitMarginService profitMarginService) {
        this.profitMarginService = profitMarginService;
    }

    @GetMapping("/gross-margin")
    public ResponseEntity<BigDecimal> getGrossMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal costOfGoodsSold) {
        return ResponseEntity.ok(profitMarginService.calculateGrossProfitMargin(totalRevenue, costOfGoodsSold));
    }

    @GetMapping("/operating-margin")
    public ResponseEntity<BigDecimal> getOperatingMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(profitMarginService.calculateOperatingProfitMargin(totalRevenue, totalExpenses));
    }

    @GetMapping("/net-margin")
    public ResponseEntity<BigDecimal> getNetMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(profitMarginService.calculateNetProfitMargin(totalRevenue, totalExpenses));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getProfitSummary(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal costOfGoodsSold,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(profitMarginService.getProfitSummary(totalRevenue, costOfGoodsSold, totalExpenses));
    }
}
