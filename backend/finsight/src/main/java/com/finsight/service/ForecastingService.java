package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.enums.TransactionType;
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

    public Map<String, Object> generateForecast(Long businessId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> forecast = new HashMap<>();

        List<Transaction> allTransactions = transactionRepository.findByBusinessIdBetweenDates(
                businessId, startDate, endDate);

        if (allTransactions.size() < 3) {
            forecast.put("error", "At least 3 months of data required for forecasting");
            return forecast;
        }

        BigDecimal totalRevenue = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long months = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
        if (months == 0) months = 1;

        BigDecimal avgRevenue = totalRevenue.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        BigDecimal avgExpenses = totalExpenses.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        BigDecimal avgProfit = avgRevenue.subtract(avgExpenses);

        BigDecimal growthFactor = new BigDecimal("1.05");

        forecast.put("projectedRevenue", avgRevenue.multiply(growthFactor).setScale(2, RoundingMode.HALF_UP));
        forecast.put("projectedExpenses", avgExpenses.multiply(growthFactor).setScale(2, RoundingMode.HALF_UP));
        forecast.put("projectedProfit", avgProfit.multiply(growthFactor).setScale(2, RoundingMode.HALF_UP));
        forecast.put("averageMonthlyRevenue", avgRevenue);
        forecast.put("averageMonthlyExpenses", avgExpenses);
        forecast.put("dataMonths", months);

        BigDecimal revenueHigh = avgRevenue.multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal revenueLow = avgRevenue.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        forecast.put("revenueRangeHigh", revenueHigh);
        forecast.put("revenueRangeLow", revenueLow);

        return forecast;
    }
}
