package com.finsight.model;

import com.finsight.model.enums.PaymentMethod;
import com.finsight.model.enums.PaymentProvider;
import com.finsight.model.enums.PosTransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One card machine payment reported by a payment provider.
 * providerTransactionId is unique, so the same payment (e.g. a repeated webhook) can never be saved twice.
 */
@Entity
@Table(name = "pos_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_pos_transactions_provider_txn_id",
                columnNames = "provider_transaction_id"),
        indexes = @Index(name = "idx_pos_transactions_business_time", columnList = "business_id, createdAt"))
@Getter
@Setter
public class PosTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_transaction_id", nullable = false)
    private String providerTransactionId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PosTransactionStatus status = PosTransactionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
