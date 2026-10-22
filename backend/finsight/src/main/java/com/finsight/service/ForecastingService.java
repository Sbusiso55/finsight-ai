package com.finsight.service;

import com.finsight.model.enums.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastingService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, BigDecimal> forecastRevenue(Long businessId, int monthsOfHistory) {
        List<BigDecimal> monthlyRevenue = getMonthlyTotals(businessId, TransactionType.INCOME, monthsOfHistory);
        BigDecimal forecast = calculateForecast(monthlyRevenue);
        BigDecimal margin = forecast.multiply(new BigDecimal("0.10"));
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("forecast", forecast);
        result.put("low", forecast.subtract(margin));
        result.put("high", forecast.add(margin));
        return result;
    }

    public Map<String, BigDecimal> forecastExpenses(Long businessId, int monthsOfHistory) {
        List<BigDecimal> monthlyExpenses = getMonthlyTotals(businessId, TransactionType.EXPENSE, monthsOfHistory);
        BigDecimal forecast = calculateForecast(monthlyExpenses);
        BigDecimal margin = forecast.multiply(new BigDecimal("0.10"));
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("forecast", forecast);
        result.put("low", forecast.subtract(margin));
        result.put("high", forecast.add(margin));
        return result;
    }

    public Map<String, BigDecimal> forecastProfit(Long businessId, int monthsOfHistory) {
        Map<String, BigDecimal> revForecast = forecastRevenue(businessId, monthsOfHistory);
        Map<String, BigDecimal> expForecast = forecastExpenses(businessId, monthsOfHistory);
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("forecast", revForecast.get("forecast").subtract(expForecast.get("forecast")));
        result.put("low", revForecast.get("low").subtract(expForecast.get("high")));
        result.put("high", revForecast.get("high").subtract(expForecast.get("low")));
        return result;
    }

    private List<BigDecimal> getMonthlyTotals(Long businessId, TransactionType type, int months) {
        List<BigDecimal> totals = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = months; i >= 1; i--) {
            LocalDate from = now.minusMonths(i).withDayOfMonth(1);
            LocalDate to = from.plusMonths(1).minusDays(1);
            BigDecimal total = transactionRepository.sumByType(businessId, type, from, to);
            totals.add(total);
        }
        return totals;
    }

    private BigDecimal calculateForecast(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (values.size() < 3) {
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal v : values) {
                sum = sum.add(v);
            }
            return sum.divide(new BigDecimal(values.size()), 2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            sum = sum.add(v);
        }
        BigDecimal average = sum.divide(new BigDecimal(values.size()), 2, RoundingMode.HALF_UP);
        BigDecimal lastMonth = values.get(values.size() - 1);
        BigDecimal secondLast = values.get(values.size() - 2);
        BigDecimal trend = lastMonth.subtract(secondLast);
        return average.add(trend);
    }
}


