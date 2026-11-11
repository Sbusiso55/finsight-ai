package com.finsight.controller;

import com.finsight.dto.ProfitMarginDTO;
import com.finsight.service.ProfitMarginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/profit")
public class ProfitMarginController {

    @Autowired
    private ProfitMarginService profitMarginService;

    @GetMapping("/gross")
    public ResponseEntity<ProfitMarginDTO> getGrossProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal costOfGoodsSold) {
        return ResponseEntity.ok(ProfitMarginDTO.from("GROSS",
                profitMarginService.calculateGrossProfitMargin(totalRevenue, costOfGoodsSold)));
    }

    @GetMapping("/operating")
    public ResponseEntity<ProfitMarginDTO> getOperatingProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(ProfitMarginDTO.from("OPERATING",
                profitMarginService.calculateOperatingProfitMargin(totalRevenue, totalExpenses)));
    }

    @GetMapping("/net")
    public ResponseEntity<ProfitMarginDTO> getNetProfitMargin(
            @RequestParam BigDecimal totalRevenue,
            @RequestParam BigDecimal totalExpenses) {
        return ResponseEntity.ok(ProfitMarginDTO.from("NET",
                profitMarginService.calculateNetProfitMargin(totalRevenue, totalExpenses)));
    }
}
