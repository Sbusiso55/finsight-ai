package com.finsight.dto;

import java.math.BigDecimal;

public record ProfitMarginDTO(String type, BigDecimal marginPercent) {
    public static ProfitMarginDTO from(String type, BigDecimal marginPercent) {
        return new ProfitMarginDTO(type, marginPercent);
    }
}
