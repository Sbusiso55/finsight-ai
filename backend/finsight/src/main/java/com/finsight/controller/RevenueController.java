
package com.finsight.controller;

import com.finsight.service.RevenueService;
import com.finsight.repository.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/total")
    public BigDecimal getTotalRevenue(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return revenueService.calculateTotalRevenue(businessId, from, to);
    }

    @GetMapping("/by-category")
    public List<TransactionRepository.CategoryTotal> getRevenueByCategory(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return revenueService.calculateRevenueByCategory(businessId, from, to);
    }

    @GetMapping("/growth")
    public BigDecimal getRevenueGrowthRate(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate currentFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate currentTo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate previousFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate previousTo) {

        return revenueService.calculateRevenueGrowthRate(
                businessId, currentFrom, currentTo, previousFrom, previousTo);
    }

    @GetMapping("/gross-profit-margin")
    public BigDecimal getGrossProfitMargin(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return revenueService.calculateGrossProfitMargin(businessId, from, to);
    }

    @GetMapping("/net-profit-margin")
    public BigDecimal getNetProfitMargin(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return revenueService.calculateNetProfitMargin(businessId, from, to);
    }
}
