package com.finsight.model;

import com.finsight.model.enums.ForecastConfidence;
import com.finsight.model.enums.ForecastMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A projection for one future month. Forecasts are estimates and are always stored as a range
 * (low / expected / high), never a single fixed number.
 */
@Entity
@Table(name = "forecasts",
        indexes = @Index(name = "idx_forecasts_business_month", columnList = "business_id, forecastMonth"))
@Getter
@Setter
public class Forecast extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    /** First day of the month being predicted, e.g. 2026-04-01. */
    @Column(nullable = false)
    private LocalDate forecastMonth;

    // Expected values (e.g. April: R34 000 / R27 700 / R6 300)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal projectedRevenue;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal projectedExpenses;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal projectedProfit;

    // Range
    @Column(precision = 15, scale = 2)
    private BigDecimal revenueLow;

    @Column(precision = 15, scale = 2)
    private BigDecimal revenueHigh;

    @Column(precision = 15, scale = 2)
    private BigDecimal expensesLow;

    @Column(precision = 15, scale = 2)
    private BigDecimal expensesHigh;

    @Column(precision = 15, scale = 2)
    private BigDecimal profitLow;

    @Column(precision = 15, scale = 2)
    private BigDecimal profitHigh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastMethod method = ForecastMethod.AVERAGE_MONTHLY_CHANGE;

    /** How many months of history the forecast used (3 is the minimum). */
    @Column(nullable = false)
    private int monthsOfHistory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastConfidence confidence;

    /** Caveat shown to the user, e.g. "Based on 3 months of data; seasonality cannot be detected yet." */
    @Column(length = 500)
    private String disclaimer;
}
