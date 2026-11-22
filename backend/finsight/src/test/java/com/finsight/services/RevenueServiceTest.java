package com.finsight.services;

import com.finsight.repository.TransactionRepository;
import com.finsight.service.RevenueService;
import com.finsight.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RevenueService revenueService;

    private final Long businessId = 1L;
    private final LocalDate from = LocalDate.of(2026, 1, 1);
    private final LocalDate to = LocalDate.of(2026, 1, 31);

    @Test
    void shouldCalculateTotalRevenue() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("1000.00"));

        BigDecimal result =
                revenueService.calculateTotalRevenue(businessId, from, to);

        assertEquals(0, result.compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void shouldReturnRevenueByCategory() {
        List<TransactionRepository.CategoryTotal> categories = List.of();

        when(transactionService.getRevenueByCategory(businessId, from, to))
                .thenReturn(categories);

        List<TransactionRepository.CategoryTotal> result =
                revenueService.calculateRevenueByCategory(businessId, from, to);

        assertEquals(categories, result);
    }

    @Test
    void shouldCalculatePositiveRevenueGrowthRate() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("120.00"));

        when(transactionService.getTotalRevenue(
                businessId, from.minusMonths(1), to.minusMonths(1)))
                .thenReturn(new BigDecimal("100.00"));

        BigDecimal result = revenueService.calculateRevenueGrowthRate(
                businessId, from, to, from.minusMonths(1), to.minusMonths(1));

        assertEquals(0, result.compareTo(new BigDecimal("20.0000")));
    }

    @Test
    void shouldReturnZeroGrowthWhenPreviousRevenueIsZero() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("100.00"));

        when(transactionService.getTotalRevenue(
                businessId, from.minusMonths(1), to.minusMonths(1)))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result = revenueService.calculateRevenueGrowthRate(
                businessId, from, to, from.minusMonths(1), to.minusMonths(1));

        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldCalculateGrossProfitMargin() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("1000.00"));

        when(transactionService.getCostOfGoodsSold(businessId, from, to))
                .thenReturn(new BigDecimal("600.00"));

        BigDecimal result =
                revenueService.calculateGrossProfitMargin(businessId, from, to);

        assertEquals(0, result.compareTo(new BigDecimal("40.0000")));
    }

    @Test
    void shouldReturnZeroGrossProfitMarginWhenRevenueIsZero() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result =
                revenueService.calculateGrossProfitMargin(businessId, from, to);

        assertEquals(0, result.compareTo(BigDecimal.ZERO));

        verify(transactionService, never())
                .getCostOfGoodsSold(businessId, from, to);
    }

    @Test
    void shouldCalculateNetProfitMargin() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(new BigDecimal("1000.00"));

        when(transactionService.getTotalExpenses(businessId, from, to))
                .thenReturn(new BigDecimal("750.00"));

        BigDecimal result =
                revenueService.calculateNetProfitMargin(businessId, from, to);

        assertEquals(0, result.compareTo(new BigDecimal("25.0000")));
    }

    @Test
    void shouldReturnZeroNetProfitMarginWhenRevenueIsZero() {
        when(transactionService.getTotalRevenue(businessId, from, to))
                .thenReturn(BigDecimal.ZERO);

        BigDecimal result =
                revenueService.calculateNetProfitMargin(businessId, from, to);

        assertEquals(0, result.compareTo(BigDecimal.ZERO));

        verify(transactionService, never())
                .getTotalExpenses(businessId, from, to);
    }
}
