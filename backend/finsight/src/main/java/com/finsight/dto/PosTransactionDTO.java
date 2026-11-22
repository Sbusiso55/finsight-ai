package com.finsight.dto;

import com.finsight.model.PosTransaction;
import com.finsight.model.enums.PaymentMethod;
import com.finsight.model.enums.PaymentProvider;
import com.finsight.model.enums.PosTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What the app shows for a card machine payment. Deliberately leaves out businessId and the
 * provider's own transaction reference: neither is needed by the screen, and neither should leak.
 */
@Getter
@AllArgsConstructor
public class PosTransactionDTO {
    private Long id;
    private PaymentProvider provider;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PosTransactionStatus status;
    private LocalDateTime createdAt;

    public static PosTransactionDTO from(PosTransaction t) {
        return new PosTransactionDTO(t.getId(), t.getProvider(), t.getAmount(),
                t.getPaymentMethod(), t.getStatus(), t.getCreatedAt());
    }
}
