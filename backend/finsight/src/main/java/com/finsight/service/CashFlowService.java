package com.finsight.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class CashFlowService {

    public BigDecimal calculateNetCashFlow(BigDecimal totalInflows, BigDecimal totalOutflows) {
        return totalInflows.subtract(totalOutflows);
    }

    public BigDecimal calculateOpeningBalance(BigDecimal previousClosingBalance) {
        return previousClosingBalance;
    }

    public BigDecimal calculateClosingBalance(BigDecimal openingBalance, BigDecimal netCashFlow) {
        return openingBalance.add(netCashFlow);
    }

    public Map<String, BigDecimal> generateCashFlowSummary(BigDecimal openingBalance, BigDecimal totalInflows, BigDecimal totalOutflows) {
        BigDecimal netCashFlow = calculateNetCashFlow(totalInflows, totalOutflows);
        BigDecimal closingBalance = calculateClosingBalance(openingBalance, netCashFlow);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("openingBalance", openingBalance);
        summary.put("totalInflows", totalInflows);
        summary.put("totalOutflows", totalOutflows);
        summary.put("netCashFlow", netCashFlow);
        summary.put("closingBalance", closingBalance);

        return summary;
    }

    public String determineCashFlowTrend(BigDecimal currentNetCashFlow, BigDecimal previousNetCashFlow) {
        int comparison = currentNetCashFlow.compareTo(previousNetCashFlow);
        if (comparison > 0) {
            return "Positive and rising";
        } else if (comparison < 0) {
            return "Declining";
        } else {
            return "Stable";
        }
    }
}
