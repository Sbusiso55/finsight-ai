package com.finsight.dto;

import java.math.BigDecimal;
import java.util.Map;

public record CashFlowDTO(
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow,
        BigDecimal closingBalance) {

    public static CashFlowDTO from(Map<String, BigDecimal> m) {
        return new CashFlowDTO(m.get("openingBalance"), m.get("totalIncome"),
                m.get("totalExpenses"), m.get("netCashFlow"), m.get("closingBalance"));
    }
}
