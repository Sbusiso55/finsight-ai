package com.finsight.controller;

import com.finsight.service.ProfitMarginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/profit")
public class ProfitMarginController {

    @Autowired
    private ProfitMarginService profitMarginService;

    @GetMapping("/gross")
    public ResponseEntity<BigDecimal> getGrossProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal costOfGoodsSold) {
        return ResponseEntity.ok(profitMarginService.calculateGrossProfitMargin(totalRevenue, costOfGoodsSold));
    }

    @GetMapping("/operating")
    public ResponseEntity<BigDecimal> getOperatingProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(profitMarginService.calculateOperatingProfitMargin(totalRevenue, totalExpenses));
    }

    @GetMapping("/net")
    public ResponseEntity<BigDecimal> getNetProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(profitMarginService.calculateNetProfitMargin(totalRevenue, totalExpenses));
    }
}
