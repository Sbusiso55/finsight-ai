package com.finsight.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pre-calculated totals for one business and one month. This is the "historical totals" table
 * (Jan / Feb / Mar) that growth rates, margins, cash flow and forecasts are built from.
 * Rebuilt by the service layer whenever transactions in that month change.
 */
@Entity
@Table(name = "monthly_summaries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "periodStart"}))
@Getter
@Setter
public class MonthlySummary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    /** First day of the month, e.g. 2026-03-01. */
    @Column(nullable = false)
    private LocalDate periodStart;

    // Revenue
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(nullable = false)
    private int revenueTransactionCount;

    // Expenses
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal operatingExpenses = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fixedCosts = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal variableCosts = BigDecimal.ZERO;

    // Cash flow
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cashInflows = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cashOutflows = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;

    /** Revenue - COGS. */
    @Transient
    public BigDecimal getGrossProfit(){
        return totalRevenue.subtract(costOfGoodsSold);
    }

    /** Gross profit - operating expenses (equals revenue - total expenses before tax). */
    @Transient
    public BigDecimal getNetProfit() {
        return totalRevenue.subtract(totalExpenses);
    }

    /** Cash inflows - cash outflows. */
    @Transient
    public BigDecimal getNetCashFlow() {
        return cashInflows.subtract(cashOutflows);
    }
}
