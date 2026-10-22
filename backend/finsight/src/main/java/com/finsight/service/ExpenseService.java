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
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal getTotalExpenses(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndTypeBetweenDates(
                businessId, TransactionType.EXPENSE, startDate, endDate);
        return expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getExpenseBreakdown(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndTypeBetweenDates(
                businessId, TransactionType.EXPENSE, startDate, endDate);
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    public Map<String, BigDecimal> getFixedVsVariableCosts(Long businessId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> expenses = transactionRepository.findByBusinessIdAndTypeBetweenDates(
                businessId, TransactionType.EXPENSE, startDate, endDate);
        Map<String, BigDecimal> result = new HashMap<>();
        BigDecimal fixed = expenses.stream()
                .filter(t -> t.getCostBehavior() != null && t.getCostBehavior().name().equals("FIXED"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variable = expenses.stream()
                .filter(t -> t.getCostBehavior() != null && t.getCostBehavior().name().equals("VARIABLE"))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("fixedCosts", fixed);
        result.put("variableCosts", variable);
        return result;
    }

    public Map<String, Object> generateExpenseReport(Long businessId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        BigDecimal totalExpenses = getTotalExpenses(businessId, startDate, endDate);
        Map<String, BigDecimal> breakdown = getExpenseBreakdown(businessId, startDate, endDate);
        Map<String, BigDecimal> fixedVsVariable = getFixedVsVariableCosts(businessId, startDate, endDate);

        report.put("totalExpenses", totalExpenses);
        report.put("breakdown", breakdown);
        report.put("fixedCosts", fixedVsVariable.get("fixedCosts"));
        report.put("variableCosts", fixedVsVariable.get("variableCosts"));

        String largestCategory = "None";
        BigDecimal largestAmount = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : breakdown.entrySet()) {
            if (entry.getValue().compareTo(largestAmount) > 0) {
                largestAmount = entry.getValue();
                largestCategory = entry.getKey();
            }
        }
        report.put("largestCategory", largestCategory);
        report.put("largestAmount", largestAmount);

        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            report.put("fixedPercentage", fixedVsVariable.get("fixedCosts")
                    .multiply(new BigDecimal("100"))
                    .divide(totalExpenses, 2, RoundingMode.HALF_UP));
            report.put("variablePercentage", fixedVsVariable.get("variableCosts")
                    .multiply(new BigDecimal("100"))
                    .divide(totalExpenses, 2, RoundingMode.HALF_UP));
        }

        return report;
    }
}
