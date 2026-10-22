package com.finsight.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MonthlySummaryService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ProfitMarginService profitMarginService;

    @Autowired
    private CashFlowService cashFlowService;

    public Map<String, Object> generateMonthlySummary(Long businessId, LocalDate startDate, LocalDate endDate, BigDecimal openingBalance) {
        BigDecimal totalRevenue = transactionService.calculateTotalRevenue(businessId, startDate, endDate);
        BigDecimal totalExpenses = transactionService.calculateTotalExpenses(businessId, startDate, endDate);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        BigDecimal profitMargin = profitMarginService.calculateNetProfitMargin(totalRevenue, totalExpenses);
        Map<String, BigDecimal> cashFlow = cashFlowService.calculateCashFlow(openingBalance, totalRevenue, totalExpenses);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);
        summary.put("profitMargin", profitMargin);
        summary.put("cashFlow", cashFlow);
        summary.put("startDate", startDate.toString());
        summary.put("endDate", endDate.toString());
        return summary;
    }
}
