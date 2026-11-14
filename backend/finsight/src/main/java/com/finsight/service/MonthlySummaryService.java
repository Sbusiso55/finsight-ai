
package com.finsight.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MonthlySummaryService {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ProfitMarginService profitMarginService;

    public Map<String, Object> generateMonthlySummary(Long businessId, LocalDate from, LocalDate to, BigDecimal openingBalance) {
        BigDecimal totalRevenue = transactionService.getTotalRevenue(businessId, from, to);
        BigDecimal totalExpenses = transactionService.getTotalExpenses(businessId, from, to);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal closingBalance = openingBalance.add(netProfit);

        BigDecimal costOfGoodsSold =
                transactionService.getCostOfGoodsSold(
                        businessId, from, to
                );

        BigDecimal grossMargin =
                profitMarginService.calculateGrossProfitMargin(
                        totalRevenue, costOfGoodsSold
                );

        BigDecimal netMargin =
                profitMarginService.calculateNetProfitMargin(
                        totalRevenue, totalExpenses
                );

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netProfit", netProfit);
        summary.put("openingBalance", openingBalance);
        summary.put("closingBalance", closingBalance);
        summary.put("grossMargin", grossMargin);
        summary.put("netMargin", netMargin);
        summary.put("expenseBreakdown", expenseService.getExpenseBreakdown(businessId, from, to));
        summary.put("fixedVsVariable", expenseService.getFixedVsVariable(businessId, from, to));
        summary.put("expenseToRevenueRatio", expenseService.getExpenseToRevenueRatio(businessId, from, to));
        return summary;
    }
}

