package com.finsight.controller;

import com.finsight.service.ForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
public class ForecastingController {

    @Autowired
    private ForecastingService forecastingService;

    @PostMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> forecastRevenue(@RequestBody List<BigDecimal> historicalRevenue) {
        return ResponseEntity.ok(forecastingService.forecastRevenue(historicalRevenue));
    }

    @PostMapping("/expenses")
    public ResponseEntity<Map<String, BigDecimal>> forecastExpenses(@RequestBody List<BigDecimal> historicalExpenses) {
        return ResponseEntity.ok(forecastingService.forecastExpenses(historicalExpenses));
    }

    @PostMapping("/profit")
    public ResponseEntity<Map<String, BigDecimal>> forecastProfit(
            @RequestBody Map<String, List<BigDecimal>> historicalData) {
        List<BigDecimal> revenue = historicalData.get("revenue");
        List<BigDecimal> expenses = historicalData.get("expenses");
        return ResponseEntity.ok(forecastingService.forecastProfit(revenue, expenses));
    }
}
