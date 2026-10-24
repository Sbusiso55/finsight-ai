
package com.finsight.service;

import org.springframework.stereotype.Service;
import com.finsight.repository.TransactionRepository;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class RevenueService {

    private final TransactionService transactionService;

    public RevenueService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public BigDecimal calculateTotalRevenue(
            Long businessId, LocalDate from, LocalDate to) {

        return transactionService.getTotalRevenue(
                businessId, from, to);
    }


    public List<TransactionRepository.CategoryTotal> calculateRevenueByCategory(
            Long businessId, LocalDate from, LocalDate to) {

        return transactionService.getRevenueByCategory(
                businessId, from, to);
    }


    public BigDecimal calculateRevenueGrowthRate(
            Long businessId,
            LocalDate currentFrom,
            LocalDate currentTo,
            LocalDate previousFrom,
            LocalDate previousTo) {

        BigDecimal currentRevenue =
                transactionService.getTotalRevenue(
                        businessId, currentFrom, currentTo);

        BigDecimal previousRevenue =
                transactionService.getTotalRevenue(
                        businessId, previousFrom, previousTo);

        if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return currentRevenue.subtract(previousRevenue)
                .divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }


    public BigDecimal calculateGrossProfitMargin(
            Long businessId, LocalDate from, LocalDate to) {

        BigDecimal revenue =
                transactionService.getTotalRevenue(
                        businessId, from, to);

        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal costOfGoodsSold =
                transactionService.getCostOfGoodsSold(
                        businessId, from, to);

        BigDecimal grossProfit =
                revenue.subtract(costOfGoodsSold);

        return grossProfit
                .divide(revenue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }


    public BigDecimal calculateNetProfitMargin(
            Long businessId, LocalDate from, LocalDate to) {

        BigDecimal revenue =
                transactionService.getTotalRevenue(
                        businessId, from, to);

        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalExpenses =
                transactionService.getTotalExpenses(
                        businessId, from, to);

        BigDecimal netProfit =
                revenue.subtract(totalExpenses);

        return netProfit
                .divide(revenue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }



}
