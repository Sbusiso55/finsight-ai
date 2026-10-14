package com.finsight.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProfitMarginService {

    public BigDecimal calculateGrossProfit(BigDecimal totalRevenue, BigDecimal costOfGoodsSold) {
        return totalRevenue.subtract(costOfGoodsSold);
    }

    public BigDecimal calculateGrossMargin(BigDecimal totalRevenue, BigDecimal costOfGoodsSold) {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal grossProfit = calculateGrossProfit(totalRevenue, costOfGoodsSold);
        return grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public BigDecimal calculateNetProfit(BigDecimal totalRevenue, BigDecimal totalExpenses) {
        return totalRevenue.subtract(totalExpenses);
    }

    public BigDecimal calculateNetMargin(BigDecimal totalRevenue, BigDecimal totalExpenses) {
        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal netProfit = calculateNetProfit(totalRevenue, totalExpenses);
        return netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public Map<String, BigDecimal> generateProfitSummary(BigDecimal totalRevenue, BigDecimal costOfGoodsSold, BigDecimal totalExpenses) {
        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("costOfGoodsSold", costOfGoodsSold);
        summary.put("totalExpenses", totalExpenses);
        summary.put("grossProfit", calculateGrossProfit(totalRevenue, costOfGoodsSold));
        summary.put("grossMargin", calculateGrossMargin(totalRevenue, costOfGoodsSold));
        summary.put("netProfit", calculateNetProfit(totalRevenue, totalExpenses));
        summary.put("netMargin", calculateNetMargin(totalRevenue, totalExpenses));

        return summary;
    }
}
