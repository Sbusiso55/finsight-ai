package com.finsight.controller;

import com.finsight.service.ForecastingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
public class ForecastingController {

    private final ForecastingService forecastingService;

    public ForecastingController(ForecastingService forecastingService) {
        this.forecastingService = forecastingService;
    }

    @PostMapping("/revenue")
    public ResponseEntity<BigDecimal> forecastRevenue(
            @RequestBody List<BigDecimal> historicalRevenue) {
        return ResponseEntity.ok(forecastingService.forecastRevenue(historicalRevenue));
    }

    @PostMapping("/expenses")
    public ResponseEntity<BigDecimal> forecastExpenses(
            @RequestBody List<BigDecimal> historicalExpenses) {
        return ResponseEntity.ok(forecastingService.forecastExpenses(historicalExpenses));
    }

    @PostMapping("/profit")
    public ResponseEntity<BigDecimal> forecastProfit(
            @RequestBody Map<String, List<BigDecimal>> data) {
        return ResponseEntity.ok(forecastingService.forecastProfit(
                data.get("revenue"), data.get("expenses")));
    }

    @PostMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> getForecastSummary(
            @RequestBody Map<String, List<BigDecimal>> data) {
        return ResponseEntity.ok(forecastingService.getForecastSummary(
                data.get("revenue"), data.get("expenses")));
    }
}
