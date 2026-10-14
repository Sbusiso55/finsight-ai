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

    @Autowired
    private ExpenseService expenseService;

    public Map<String, Object> generateMonthlySummary(Long businessId, LocalDate startDate, LocalDate endDate, BigDecimal openingBalance, BigDecimal costOfGoodsSold) {
        Map<String, Object> summary = new HashMap<>();

        BigDecimal totalRevenue = transactionService.getTotalRevenue(businessId, startDate, endDate);
        BigDecimal totalExpenses = transactionService.getTotalExpenses(businessId, startDate, endDate);

        BigDecimal netCashFlow = cashFlowService.calculateNetCashFlow(totalRevenue, totalExpenses);
        BigDecimal closingBalance = cashFlowService.calculateClosingBalance(openingBalance, netCashFlow);

        BigDecimal grossProfit = profitMarginService.calculateGrossProfit(totalRevenue, costOfGoodsSold);
        BigDecimal grossMargin = profitMarginService.calculateGrossMargin(totalRevenue, costOfGoodsSold);
        BigDecimal netProfit = profitMarginService.calculateNetProfit(totalRevenue, totalExpenses);
        BigDecimal netMargin = profitMarginService.calculateNetMargin(totalRevenue, totalExpenses);

        BigDecimal expenseToRevenueRatio = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) != 0) {
            expenseToRevenueRatio = totalExpenses.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        summary.put("month", startDate.getMonth().toString());
        summary.put("year", startDate.getYear());
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpenses", totalExpenses);
        summary.put("grossProfit", grossProfit);
        summary.put("grossMargin", grossMargin);
        summary.put("netProfit", netProfit);
        summary.put("netMargin", netMargin);
        summary.put("openingBalance", openingBalance);
        summary.put("netCashFlow", netCashFlow);
        summary.put("closingBalance", closingBalance);
        summary.put("expenseToRevenueRatio", expenseToRevenueRatio);

        return summary;
    }
}
