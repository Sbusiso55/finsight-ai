package com.finsight.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CashFlowService {

    public Map<String, BigDecimal> calculateCashFlow(BigDecimal openingBalance, BigDecimal totalIncome, BigDecimal totalExpenses) {
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        BigDecimal closingBalance = openingBalance.add(netCashFlow);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("openingBalance", openingBalance);
        result.put("totalIncome", totalIncome);
        result.put("totalExpenses", totalExpenses);
        result.put("netCashFlow", netCashFlow);
        result.put("closingBalance", closingBalance);
        return result;
    }
}
