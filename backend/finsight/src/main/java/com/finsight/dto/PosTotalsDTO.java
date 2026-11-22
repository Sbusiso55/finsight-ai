package com.finsight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** The "total amount paid so far" figure shown beside the live payments list. */
@Getter
@AllArgsConstructor
public class PosTotalsDTO {
    private BigDecimal totalPaid;
    private long successfulPayments;

    public static PosTotalsDTO of(BigDecimal totalPaid, long successfulPayments) {
        return new PosTotalsDTO(totalPaid, successfulPayments);
    }
}
