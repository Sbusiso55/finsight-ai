package com.finsight.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BusinessRequest {
    private String name;
    private String industry;
    private String currency;
    private BigDecimal openingBalance;
    private LocalDate openingBalanceDate;
    private boolean seasonal;
}