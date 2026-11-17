package com.finsight.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** The three calculated getters on MonthlySummary (the figures from the financial-model document). */
class MonthlySummaryTest {

    private MonthlySummary summary() {
        MonthlySummary s = new MonthlySummary();
        s.setTotalRevenue(new BigDecimal("25500.00"));
        s.setCostOfGoodsSold(new BigDecimal("6000.00"));
        s.setOperatingExpenses(new BigDecimal("18750.00"));
        s.setTotalExpenses(new BigDecimal("24750.00"));
        s.setCashInflows(new BigDecimal("25500.00"));
        s.setCashOutflows(new BigDecimal("24750.00"));
        return s;
    }

    @Test
    void grossProfitIsRevenueMinusCostOfGoodsSold() {
        assertThat(summary().getGrossProfit()).isEqualByComparingTo("19500.00");
    }

    @Test
    void netProfitIsRevenueMinusTotalExpenses() {
        assertThat(summary().getNetProfit()).isEqualByComparingTo("750.00");
    }

    @Test
    void netCashFlowIsInflowsMinusOutflows() {
        assertThat(summary().getNetCashFlow()).isEqualByComparingTo("750.00");
    }

    @Test
    void lossGivesNegativeNetProfit() {
        MonthlySummary s = summary();
        s.setTotalExpenses(new BigDecimal("30000.00"));
        assertThat(s.getNetProfit()).isEqualByComparingTo("-4500.00");
    }
}
