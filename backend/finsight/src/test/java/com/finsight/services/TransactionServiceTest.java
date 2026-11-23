
package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.model.enums.CostClassification;
import com.finsight.model.enums.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private final Long businessId = 1L;
    private final Long transactionId = 10L;
    private final LocalDate from = LocalDate.of(2026, 1, 1);
    private final LocalDate to = LocalDate.of(2026, 1, 31);

    @Test
    void shouldFindTransactionsByBusinessId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> expected = new PageImpl<>(List.of());

        when(transactionRepository.findByBusinessId(businessId, pageable))
                .thenReturn(expected);

        Page<Transaction> result =
                transactionService.findByBusinessId(businessId, pageable);

        assertSame(expected, result);
    }

    @Test
    void shouldFindTransactionsByBusinessIdAndDateRange() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> expected = new PageImpl<>(List.of());

        when(transactionRepository.findByBusinessIdAndTransactionDateBetween(
                businessId, from, to, pageable)).thenReturn(expected);

        Page<Transaction> result =
                transactionService.findByBusinessIdAndDateRange(
                        businessId, from, to, pageable);

        assertSame(expected, result);
    }

    @Test
    void shouldFindTransactionsByBusinessIdAndDateRangeAsList() {
        List<Transaction> expected = List.of();

        when(transactionRepository
                .findByBusinessIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                        businessId, from, to))
                .thenReturn(expected);

        List<Transaction> result =
                transactionService.findByBusinessIdAndDateRangeList(
                        businessId, from, to);

        assertSame(expected, result);
    }

    @Test
    void shouldFindTransactionByIdAndBusinessId() {
        Transaction transaction = new Transaction();

        when(transactionRepository.findByIdAndBusinessId(
                transactionId, businessId))
                .thenReturn(Optional.of(transaction));

        Optional<Transaction> result =
                transactionService.findByIdAndBusinessId(
                        transactionId, businessId);

        assertTrue(result.isPresent());
        assertSame(transaction, result.get());
    }

    @Test
    void shouldReturnEmptyWhenTransactionIsNotFound() {
        when(transactionRepository.findByIdAndBusinessId(
                transactionId, businessId))
                .thenReturn(Optional.empty());

        Optional<Transaction> result =
                transactionService.findByIdAndBusinessId(
                        transactionId, businessId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveTransaction() {
        Transaction transaction = new Transaction();

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.save(transaction);

        assertSame(transaction, result);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldDeleteTransactionById() {
        transactionService.deleteById(transactionId);

        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void shouldGetTotalRevenue() {
        BigDecimal expected = new BigDecimal("1000.00");

        when(transactionRepository.sumByType(
                businessId, TransactionType.INCOME, from, to))
                .thenReturn(expected);

        BigDecimal result =
                transactionService.getTotalRevenue(businessId, from, to);

        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void shouldGetTotalExpenses() {
        BigDecimal expected = new BigDecimal("500.00");

        when(transactionRepository.sumByType(
                businessId, TransactionType.EXPENSE, from, to))
                .thenReturn(expected);

        BigDecimal result =
                transactionService.getTotalExpenses(businessId, from, to);

        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void shouldGetRevenueByCategory() {
        TransactionRepository.CategoryTotal category =
                mock(TransactionRepository.CategoryTotal.class);

        List<TransactionRepository.CategoryTotal> expected =
                List.of(category);

        when(transactionRepository.totalsByCategory(
                businessId, TransactionType.INCOME, from, to))
                .thenReturn(expected);

        List<TransactionRepository.CategoryTotal> result =
                transactionService.getRevenueByCategory(businessId, from, to);

        assertSame(expected, result);
    }

    @Test
    void shouldGetExpensesByCategory() {
        TransactionRepository.CategoryTotal category =
                mock(TransactionRepository.CategoryTotal.class);

        List<TransactionRepository.CategoryTotal> expected =
                List.of(category);

        when(transactionRepository.totalsByCategory(
                businessId, TransactionType.EXPENSE, from, to))
                .thenReturn(expected);

        List<TransactionRepository.CategoryTotal> result =
                transactionService.getExpensesByCategory(businessId, from, to);

        assertSame(expected, result);
    }

    @Test
    void shouldGetCostOfGoodsSold() {
        BigDecimal expected = new BigDecimal("300.00");

        when(transactionRepository.sumByCostClassification(
                businessId, CostClassification.COST_OF_GOODS_SOLD, from, to))
                .thenReturn(expected);

        BigDecimal result =
                transactionService.getCostOfGoodsSold(businessId, from, to);

        assertEquals(0, result.compareTo(expected));
    }
}
