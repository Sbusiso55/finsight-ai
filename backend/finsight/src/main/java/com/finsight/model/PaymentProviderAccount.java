package com.finsight.model;

import com.finsight.model.enums.PaymentProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A business's connection to a payment provider. encryptedApiKey holds the key already encrypted
 * (never plain text) and must never appear in any DTO, log line or toString().
 */
@Entity
@Table(name = "payment_provider_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_accounts_business_provider",
                columnNames = {"business_id", "provider"}))
@Getter
@Setter
public class PaymentProviderAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private PaymentProvider provider;

    @Column(nullable = false, length = 1000)
    private String encryptedApiKey;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @PrePersist
    void onCreate() {
        if (connectedAt == null) connectedAt = LocalDateTime.now();
    }
}
