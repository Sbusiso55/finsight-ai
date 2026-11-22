package com.finsight.dto;

import com.finsight.model.Subscription;
import com.finsight.model.enums.PlanType;
import com.finsight.model.enums.SubscriptionStatus;

import java.time.Duration;
import java.time.LocalDateTime;

/** What the app needs to show the plan, trial banner and locked features. */
public record SubscriptionDTO(
        PlanType plan,
        SubscriptionStatus status,
        boolean pro,
        LocalDateTime trialEndsAt,
        LocalDateTime currentPeriodEnd,
        long daysLeft
) {
    public static SubscriptionDTO from(Subscription s) {
        boolean pro = s.getPlan() == PlanType.PRO
                && (s.getStatus() == SubscriptionStatus.TRIAL || s.getStatus() == SubscriptionStatus.ACTIVE);
        LocalDateTime end = s.getStatus() == SubscriptionStatus.TRIAL ? s.getTrialEndsAt() : s.getCurrentPeriodEnd();
        long days = end == null ? 0 : Math.max(0, (long) Math.ceil(Duration.between(LocalDateTime.now(), end).toHours() / 24.0));
        return new SubscriptionDTO(s.getPlan(), s.getStatus(), pro, s.getTrialEndsAt(), s.getCurrentPeriodEnd(), days);
    }
}