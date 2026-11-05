package com.finsight.dto;

import com.finsight.model.Forecast;
import com.finsight.model.enums.ForecastConfidence;
import com.finsight.model.enums.ForecastMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ForecastDTO {

    private LocalDate forecastMonth;
    private BigDecimal projectedRevenue;
    private BigDecimal revenueLow;
    private BigDecimal revenueHigh;
    private BigDecimal projectedExpenses;
    private BigDecimal expensesLow;
    private BigDecimal expensesHigh;
    private BigDecimal projectedProfit;
    private BigDecimal profitLow;
    private BigDecimal profitHigh;
    private ForecastMethod method;
    private int monthsOfHistory;
    private ForecastConfidence confidence;
    private String disclaimer;
    private Instant generatedAt;

    public static ForecastDTO from(Forecast forecast) {
        return new ForecastDTO(
                forecast.getForecastMonth(),
                forecast.getProjectedRevenue(),
                forecast.getRevenueLow(),
                forecast.getRevenueHigh(),
                forecast.getProjectedExpenses(),
                forecast.getExpensesLow(),
                forecast.getExpensesHigh(),
                forecast.getProjectedProfit(),
                forecast.getProfitLow(),
                forecast.getProfitHigh(),
                forecast.getMethod(),
                forecast.getMonthsOfHistory(),
                forecast.getConfidence(),
                forecast.getDisclaimer(),
                forecast.getCreatedAt());
    }
}
