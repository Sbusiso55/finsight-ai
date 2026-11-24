package com.finsight.model;

import com.finsight.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** One Pro plan purchase. The reference is unique, so a payment can only activate Pro once. */
@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private Integer months;

    /** Amount in cents, e.g. 119900 = R1 199,00. */
    @Column(nullable = false)
    private Long amountCents;

    @Column(nullable = false, length = 3)
    private String currency = "ZAR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private LocalDateTime paidAt;
}