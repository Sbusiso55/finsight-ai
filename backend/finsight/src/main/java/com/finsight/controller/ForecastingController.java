package com.finsight.controller;

import com.finsight.service.ForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/forecasting")
public class ForecastingController {

    @Autowired
    private ForecastingService forecastingService;

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> forecastRevenue(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(forecastingService.forecastRevenue(businessId, months));
    }

    @GetMapping("/expenses")
    public ResponseEntity<Map<String, BigDecimal>> forecastExpenses(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(forecastingService.forecastExpenses(businessId, months));
    }

    @GetMapping("/profit")
    public ResponseEntity<Map<String, BigDecimal>> forecastProfit(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(forecastingService.forecastProfit(businessId, months));
    }
}

