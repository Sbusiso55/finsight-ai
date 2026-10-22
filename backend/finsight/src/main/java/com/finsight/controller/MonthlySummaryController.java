package com.finsight.controller;

import com.finsight.service.MonthlySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/summary")
public class MonthlySummaryController {

    @Autowired
    private MonthlySummaryService monthlySummaryService;

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @RequestParam Long businessId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam BigDecimal openingBalance,
            @RequestParam BigDecimal previousRevenue) {
        return ResponseEntity.ok(monthlySummaryService.generateMonthlySummary(
                businessId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                openingBalance,
                previousRevenue));
    }
}
