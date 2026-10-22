package com.finsight.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProfitMarginService {

    public BigDecimal calculateGrossProfitMargin(BigDecimal revenue, BigDecimal costOfGoods) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal grossProfit = revenue.subtract(costOfGoods);
        return grossProfit.multiply(new BigDecimal("100"))
                .divide(revenue, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateOperatingProfitMargin(BigDecimal revenue, BigDecimal operatingExpenses) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal operatingProfit = revenue.subtract(operatingExpenses);
        return operatingProfit.multiply(new BigDecimal("100"))
                .divide(revenue, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateNetProfitMargin(BigDecimal revenue, BigDecimal totalExpenses) {
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal netProfit = revenue.subtract(totalExpenses);
        return netProfit.multiply(new BigDecimal("100"))
                .divide(revenue, 2, RoundingMode.HALF_UP);
    }
}
