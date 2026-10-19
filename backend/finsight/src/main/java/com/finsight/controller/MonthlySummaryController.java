package com.finsight.controller;

import com.finsight.model.MonthlySummary;
import com.finsight.service.MonthlySummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/monthly-summary")
public class MonthlySummaryController {

    private final MonthlySummaryService monthlySummaryService;

    public MonthlySummaryController(MonthlySummaryService monthlySummaryService) {
        this.monthlySummaryService = monthlySummaryService;
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, BigDecimal>> generateSummary(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses,
            @RequestParam BigDecimal openingBalance) {
        return ResponseEntity.ok(monthlySummaryService.generateMonthlySummary(
                totalRevenue, totalExpenses, openingBalance));
    }
}
