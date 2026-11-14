package com.finsight.dto;

import com.finsight.model.MonthlySummary;
import com.finsight.util.Calc;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySummaryDTO {

    private LocalDate month;
    private BigDecimal totalRevenue;
    private int revenueTransactionCount;
    private BigDecimal costOfGoodsSold;
    private BigDecimal operatingExpenses;
    private BigDecimal totalExpenses;
    private BigDecimal fixedCosts;
    private BigDecimal variableCosts;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private BigDecimal netProfitMarginPercent;
    private BigDecimal cashInflows;
    private BigDecimal cashOutflows;
    private BigDecimal netCashFlow;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;

    public static MonthlySummaryDTO from(MonthlySummary summary) {
        return new MonthlySummaryDTO(
                summary.getPeriodStart(),
                summary.getTotalRevenue(),
                summary.getRevenueTransactionCount(),
                summary.getCostOfGoodsSold(),
                summary.getOperatingExpenses(),
                summary.getTotalExpenses(),
                summary.getFixedCosts(),
                summary.getVariableCosts(),
                summary.getGrossProfit(),
                summary.getNetProfit(),
                Calc.percentOf(summary.getNetProfit(), summary.getTotalRevenue()),
                summary.getCashInflows(),
                summary.getCashOutflows(),
                summary.getNetCashFlow(),
                summary.getOpeningBalance(),
                summary.getClosingBalance());
    }
}
