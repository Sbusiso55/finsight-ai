package com.finsight.model;

import com.finsight.model.enums.PlanType;
import com.finsight.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** One per user. Starts as a 30-day Pro trial, then FREE unless paid. */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType plan = PlanType.PRO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(nullable = false)
    private LocalDateTime trialEndsAt;

    /** End of the paid period. Null until the user pays. */
    private LocalDateTime currentPeriodEnd;

    /** 1, 3, 6 or 12 once paid. */
    private Integer billingMonths;
}