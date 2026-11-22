package com.finsight.service;

import com.finsight.model.Subscription;
import com.finsight.model.User;
import com.finsight.model.enums.PlanType;
import com.finsight.model.enums.SubscriptionStatus;
import com.finsight.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    public static final int TRIAL_DAYS = 30;

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** Returns the user's subscription, creating the 30-day trial on first use and expiring it when due. */
    @Transactional
    public Subscription getCurrent(User user) {
        Subscription sub = subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> startTrial(user));
        return refresh(sub);
    }

    public boolean isPro(User user) {
        Subscription sub = getCurrent(user);
        return sub.getPlan() == PlanType.PRO
                && (sub.getStatus() == SubscriptionStatus.TRIAL || sub.getStatus() == SubscriptionStatus.ACTIVE);
    }

    private Subscription startTrial(User user) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlan(PlanType.PRO);
        sub.setStatus(SubscriptionStatus.TRIAL);
        sub.setTrialEndsAt(LocalDateTime.now().plusDays(TRIAL_DAYS));
        return subscriptionRepository.save(sub);
    }

    /** Drops expired trials and paid periods to FREE. Data is never touched. */
    private Subscription refresh(Subscription sub) {
        LocalDateTime now = LocalDateTime.now();
        boolean trialOver = sub.getStatus() == SubscriptionStatus.TRIAL && now.isAfter(sub.getTrialEndsAt());
        boolean paidOver = sub.getStatus() == SubscriptionStatus.ACTIVE
                && sub.getCurrentPeriodEnd() != null && now.isAfter(sub.getCurrentPeriodEnd());
        if (trialOver || paidOver) {
            sub.setPlan(PlanType.FREE);
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub = subscriptionRepository.save(sub);
        }
        return sub;
    }
}