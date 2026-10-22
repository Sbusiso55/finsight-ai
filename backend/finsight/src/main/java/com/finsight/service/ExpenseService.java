
package com.finsight.service;

import com.finsight.model.enums.CostBehavior;
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
public class ExpenseService {

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal getTotalExpenses(Long businessId, LocalDate from, LocalDate to) {
        return transactionRepository.sumByType(businessId, TransactionType.EXPENSE, from, to);
    }

    public Map<String, BigDecimal> getExpenseBreakdown(Long businessId, LocalDate from, LocalDate to) {
        List<TransactionRepository.CategoryTotal> totals = transactionRepository.totalsByCategory(
                businessId, TransactionType.EXPENSE, from, to);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        for (TransactionRepository.CategoryTotal ct : totals) {
            breakdown.put(ct.getCategoryName(), ct.getTotal());
        }
        return breakdown;
    }

    public Map<String, BigDecimal> getExpenseBreakdownWithPercentages(Long businessId, LocalDate from, LocalDate to) {
        Map<String, BigDecimal> breakdown = getExpenseBreakdown(businessId, from, to);
        BigDecimal total = getTotalExpenses(businessId, from, to);
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : breakdown.entrySet()) {
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : entry.getValue().multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
            result.put(entry.getKey(), percentage);
        }
        return result;
    }

    public Map<String, BigDecimal> getFixedVsVariable(Long businessId, LocalDate from, LocalDate to) {
        BigDecimal fixed = transactionRepository.sumByCostBehavior(businessId, CostBehavior.FIXED, from, to);
        BigDecimal variable = transactionRepository.sumByCostBehavior(businessId, CostBehavior.VARIABLE, from, to);
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("fixed", fixed);
        result.put("variable", variable);
        return result;
    }

    public BigDecimal getExpenseToRevenueRatio(Long businessId, LocalDate from, LocalDate to) {
        BigDecimal expenses = getTotalExpenses(businessId, from, to);
        BigDecimal revenue = transactionRepository.sumByType(businessId, TransactionType.INCOME, from, to);
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return expenses.multiply(new BigDecimal("100")).divide(revenue, 2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> generateExpenseReport(Long businessId, LocalDate from, LocalDate to) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalExpenses", getTotalExpenses(businessId, from, to));
        report.put("breakdown", getExpenseBreakdown(businessId, from, to));
        report.put("breakdownPercentages", getExpenseBreakdownWithPercentages(businessId, from, to));
        report.put("fixedVsVariable", getFixedVsVariable(businessId, from, to));
        report.put("expenseToRevenueRatio", getExpenseToRevenueRatio(businessId, from, to));
        return report;
    }
}

