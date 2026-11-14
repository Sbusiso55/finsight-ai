package com.finsight.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ForecastRangeDTO(String metric, BigDecimal forecast, BigDecimal low, BigDecimal high) {
    public static ForecastRangeDTO from(String metric, Map<String, BigDecimal> m) {
        return new ForecastRangeDTO(metric, m.get("forecast"), m.get("low"), m.get("high"));
    }
}
