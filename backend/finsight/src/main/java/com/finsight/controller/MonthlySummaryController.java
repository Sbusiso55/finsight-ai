
package com.finsight.controller;

import com.finsight.service.MonthlySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/monthly-summary")
public class MonthlySummaryController {

    @Autowired
    private MonthlySummaryService monthlySummaryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam BigDecimal openingBalance) {
        return ResponseEntity.ok(monthlySummaryService.generateMonthlySummary(businessId, from, to, openingBalance));
    }
}

