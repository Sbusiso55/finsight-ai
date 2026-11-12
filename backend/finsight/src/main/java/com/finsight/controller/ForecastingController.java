package com.finsight.controller;

import com.finsight.dto.ForecastRangeDTO;
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
    public ResponseEntity<ForecastRangeDTO> forecastRevenue(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ForecastRangeDTO.from("REVENUE",
                forecastingService.forecastRevenue(businessId, months)));
    }

    @GetMapping("/expenses")
    public ResponseEntity<ForecastRangeDTO> forecastExpenses(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ForecastRangeDTO.from("EXPENSES",
                forecastingService.forecastExpenses(businessId, months)));
    }

    @GetMapping("/profit")
    public ResponseEntity<ForecastRangeDTO> forecastProfit(
            @RequestParam Long businessId,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ForecastRangeDTO.from("PROFIT",
                forecastingService.forecastProfit(businessId, months)));
    }
}

