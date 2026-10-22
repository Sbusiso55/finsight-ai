package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastingService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, BigDecimal> forecastRevenue(Long businessId, int monthsOfHistory) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(monthsOfHistory);

        List<Transaction> transactions = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);

        BigDecimal totalRevenue = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgMonthlyRevenue = monthsOfHistory > 0
                ? totalRevenue.divide(new BigDecimal(monthsOfHistory), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, BigDecimal> forecast = new HashMap<>();
        forecast.put("averageMonthlyRevenue", avgMonthlyRevenue);
        forecast.put("forecastedRevenue", avgMonthlyRevenue.multiply(new BigDecimal("1.1")));
        return forecast;
    }

    public Map<String, BigDecimal> forecastExpenses(Long businessId, int monthsOfHistory) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(monthsOfHistory);

        List<Transaction> transactions = transactionRepository.findByBusinessIdAndDateBetween(businessId, startDate, endDate);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgMonthlyExpenses = monthsOfHistory > 0
                ? totalExpenses.divide(new BigDecimal(monthsOfHistory), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, BigDecimal> forecast = new HashMap<>();
        forecast.put("averageMonthlyExpenses", avgMonthlyExpenses);
        forecast.put("forecastedExpenses", avgMonthlyExpenses.multiply(new BigDecimal("1.05")));
        return forecast;
    }

    public Map<String, BigDecimal> forecastProfit(Long businessId, int monthsOfHistory) {
        Map<String, BigDecimal> revenueForecast = forecastRevenue(businessId, monthsOfHistory);
        Map<String, BigDecimal> expenseForecast = forecastExpenses(businessId, monthsOfHistory);

        BigDecimal forecastedRevenue = revenueForecast.get("forecastedRevenue");
        BigDecimal forecastedExpenses = expenseForecast.get("forecastedExpenses");
        BigDecimal forecastedProfit = forecastedRevenue.subtract(forecastedExpenses);

        Map<String, BigDecimal> forecast = new HashMap<>();
        forecast.put("forecastedRevenue", forecastedRevenue);
        forecast.put("forecastedExpenses", forecastedExpenses);
        forecast.put("forecastedProfit", forecastedProfit);
        return forecast;
    }
}
