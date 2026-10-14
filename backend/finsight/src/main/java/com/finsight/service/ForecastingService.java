package com.finsight.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastingService {

    private static final int MINIMUM_MONTHS_FOR_FORECAST = 3;
    private static final BigDecimal VARIATION = new BigDecimal("0.10");

    public boolean canForecast(List<BigDecimal> historicalValues) {
        return historicalValues != null && historicalValues.size() >= MINIMUM_MONTHS_FOR_FORECAST;
    }

    public BigDecimal forecastNextMonth(List<BigDecimal> historicalValues) {
        if (!canForecast(historicalValues)) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalChange = BigDecimal.ZERO;
        for (int i = 1; i < historicalValues.size(); i++) {
            totalChange = totalChange.add(historicalValues.get(i).subtract(historicalValues.get(i - 1)));
        }

        BigDecimal averageChange = totalChange.divide(
                new BigDecimal(historicalValues.size() - 1), 2, RoundingMode.HALF_UP
        );

        return historicalValues.get(historicalValues.size() - 1).add(averageChange);
    }

    public Map<String, BigDecimal> calculateForecastRange(BigDecimal forecastValue) {
        Map<String, BigDecimal> range = new HashMap<>();
        range.put("lower", forecastValue.multiply(BigDecimal.ONE.subtract(VARIATION)));
        range.put("upper", forecastValue.multiply(BigDecimal.ONE.add(VARIATION)));
        return range;
    }

    public Map<String, Object> generateFullForecast(List<BigDecimal> historicalRevenue, List<BigDecimal> historicalExpenses) {
        Map<String, Object> forecast = new HashMap<>();

        if (!canForecast(historicalRevenue) || !canForecast(historicalExpenses)) {
            forecast.put("error", "Need at least " + MINIMUM_MONTHS_FOR_FORECAST + " months of data to generate a forecast");
            return forecast;
        }

        BigDecimal forecastRevenue = forecastNextMonth(historicalRevenue);
        BigDecimal forecastExpenses = forecastNextMonth(historicalExpenses);
        BigDecimal forecastProfit = forecastRevenue.subtract(forecastExpenses);

        Map<String, BigDecimal> revenueRange = calculateForecastRange(forecastRevenue);
        Map<String, BigDecimal> expenseRange = calculateForecastRange(forecastExpenses);

        Map<String, BigDecimal> profitRange = new HashMap<>();
        profitRange.put("lower", revenueRange.get("lower").subtract(expenseRange.get("upper")));
        profitRange.put("upper", revenueRange.get("upper").subtract(expenseRange.get("lower")));

        forecast.put("forecastRevenue", forecastRevenue);
        forecast.put("forecastExpenses", forecastExpenses);
        forecast.put("forecastProfit", forecastProfit);
        forecast.put("revenueRange", revenueRange);
        forecast.put("expenseRange", expenseRange);
        forecast.put("profitRange", profitRange);

        return forecast;
    }
}

