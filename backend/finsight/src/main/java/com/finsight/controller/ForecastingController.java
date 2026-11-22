package com.finsight.controller;

import com.finsight.dto.ForecastRangeDTO;
import com.finsight.security.BusinessAccess;
import com.finsight.security.ProAccess;
import com.finsight.service.ForecastingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/** Forecasts are a Pro feature, and only for the owner's own business. */
@RestController
@RequestMapping("/api/forecasting")
public class ForecastingController {

    private final ForecastingService forecastingService;
    private final BusinessAccess businessAccess;
    private final ProAccess proAccess;

    public ForecastingController(ForecastingService forecastingService,
                                 BusinessAccess businessAccess,
                                 ProAccess proAccess) {
        this.forecastingService = forecastingService;
        this.businessAccess = businessAccess;
        this.proAccess = proAccess;
    }

    private void check(Long businessId, UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        proAccess.requirePro(user.getUsername());
    }

    @GetMapping("/revenue")
    public ResponseEntity<ForecastRangeDTO> forecastRevenue(@RequestParam Long businessId,
                                                            @RequestParam(defaultValue = "3") int months,
                                                            @AuthenticationPrincipal UserDetails user) {
        check(businessId, user);
        return ResponseEntity.ok(ForecastRangeDTO.from("REVENUE",
                forecastingService.forecastRevenue(businessId, months)));
    }

    @GetMapping("/expenses")
    public ResponseEntity<ForecastRangeDTO> forecastExpenses(@RequestParam Long businessId,
                                                             @RequestParam(defaultValue = "3") int months,
                                                             @AuthenticationPrincipal UserDetails user) {
        check(businessId, user);
        return ResponseEntity.ok(ForecastRangeDTO.from("EXPENSES",
                forecastingService.forecastExpenses(businessId, months)));
    }

    @GetMapping("/profit")
    public ResponseEntity<ForecastRangeDTO> forecastProfit(@RequestParam Long businessId,
                                                           @RequestParam(defaultValue = "3") int months,
                                                           @AuthenticationPrincipal UserDetails user) {
        check(businessId, user);
        return ResponseEntity.ok(ForecastRangeDTO.from("PROFIT",
                forecastingService.forecastProfit(businessId, months)));
    }
}