package com.finsight.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Frontend shape of ExpenseService.generateExpenseReport(). */
public record ExpenseReportDTO(
        BigDecimal totalExpenses,
        Map<String, BigDecimal> breakdown,
        Map<String, BigDecimal> breakdownPercentages,
        BigDecimal fixedCosts,
        BigDecimal variableCosts,
        BigDecimal expenseToRevenuePercent) {

    @SuppressWarnings("unchecked")
    public static ExpenseReportDTO from(Map<String, Object> report) {
        Map<String, BigDecimal> fv = (Map<String, BigDecimal>) report.get("fixedVsVariable");
        return new ExpenseReportDTO(
                (BigDecimal) report.get("totalExpenses"),
                (Map<String, BigDecimal>) report.get("breakdown"),
                (Map<String, BigDecimal>) report.get("breakdownPercentages"),
                fv.get("fixed"),
                fv.get("variable"),
                (BigDecimal) report.get("expenseToRevenueRatio"));
    }
}
