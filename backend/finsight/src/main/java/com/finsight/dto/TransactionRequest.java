package com.finsight.dto;

import com.finsight.model.enums.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {
    private Long businessId;
    private Long categoryId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
}