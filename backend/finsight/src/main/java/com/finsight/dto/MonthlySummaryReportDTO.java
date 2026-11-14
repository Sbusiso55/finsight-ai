package com.finsight.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Frontend shape of MonthlySummaryService.generateMonthlySummary(). */
public record MonthlySummaryReportDTO(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal grossMargin,
        BigDecimal netMargin,
        Map<String, BigDecimal> expenseBreakdown,
        BigDecimal fixedCosts,
        BigDecimal variableCosts,
        BigDecimal expenseToRevenuePercent) {

    @SuppressWarnings("unchecked")
    public static MonthlySummaryReportDTO from(Map<String, Object> s) {
        Map<String, BigDecimal> fv = (Map<String, BigDecimal>) s.get("fixedVsVariable");
        return new MonthlySummaryReportDTO(
                (BigDecimal) s.get("totalRevenue"), (BigDecimal) s.get("totalExpenses"),
                (BigDecimal) s.get("netProfit"), (BigDecimal) s.get("openingBalance"),
                (BigDecimal) s.get("closingBalance"), (BigDecimal) s.get("grossMargin"),
                (BigDecimal) s.get("netMargin"),
                (Map<String, BigDecimal>) s.get("expenseBreakdown"),
                fv.get("fixed"), fv.get("variable"),
                (BigDecimal) s.get("expenseToRevenueRatio"));
    }
}
