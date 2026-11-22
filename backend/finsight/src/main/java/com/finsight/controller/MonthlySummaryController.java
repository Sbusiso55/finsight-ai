package com.finsight.controller;

import com.finsight.dto.MonthlySummaryReportDTO;
import com.finsight.security.BusinessAccess;
import com.finsight.service.MonthlySummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Monthly summary stays free, but only for the owner's own business. */
@RestController
@RequestMapping("/api/monthly-summary")
public class MonthlySummaryController {

    private final MonthlySummaryService monthlySummaryService;
    private final BusinessAccess businessAccess;

    public MonthlySummaryController(MonthlySummaryService monthlySummaryService,
                                    BusinessAccess businessAccess) {
        this.monthlySummaryService = monthlySummaryService;
        this.businessAccess = businessAccess;
    }

    @GetMapping
    public ResponseEntity<MonthlySummaryReportDTO> getMonthlySummary(
            @RequestParam Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam BigDecimal openingBalance,
            @AuthenticationPrincipal UserDetails user) {
        businessAccess.requireOwned(businessId, user.getUsername());
        return ResponseEntity.ok(MonthlySummaryReportDTO.from(
                monthlySummaryService.generateMonthlySummary(businessId, from, to, openingBalance)));
    }
}