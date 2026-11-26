package com.finsight.repository;

import com.finsight.model.Subscription;
import com.finsight.model.User;
import com.finsight.model.enums.PlanType;
import com.finsight.model.enums.SubscriptionStatus;
import com.finsight.testsupport.JpaTest;
import com.finsight.testsupport.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static com.finsight.testsupport.InventoryTestData.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

/** Subscription: one row per user, found by user id, with the trial / free-plan fields stored correctly. */
@JpaTest
class SubscriptionRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired SubscriptionRepository subscriptions;
    @Autowired JdbcTemplate jdbc;

    TestData data;
    User anita, bongani;
    static final LocalDateTime TRIAL_END = LocalDateTime.of(2026, 12, 25, 9, 0);

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        anita = data.user("anita@example.com");
        bongani = data.user("bongani@example.com");
    }

    /** A new 30-day Pro trial for the user, saved. */
    private Subscription trialFor(User user) {
        Subscription s = new Subscription();
        s.setUser(user);
        s.setTrialEndsAt(TRIAL_END);
        return em.persistAndFlush(s);
    }

    // ---- defaults

    @Test
    void newSubscriptionStartsAsAProTrial() {
        Subscription s = new Subscription();
        assertThat(s.getPlan()).isEqualTo(PlanType.PRO);
        assertThat(s.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
        assertThat(s.getCurrentPeriodEnd()).isNull();
        assertThat(s.getBillingMonths()).isNull();
    }

    @Test
    void trialDefaultsAreStoredInTheDatabase() {
        Subscription saved = trialFor(anita);
        em.clear();
        Subscription loaded = subscriptions.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getPlan()).isEqualTo(PlanType.PRO);
        assertThat(loaded.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
        assertThat(loaded.getTrialEndsAt()).isEqualTo(TRIAL_END);
        assertThat(loaded.getCurrentPeriodEnd()).isNull();
        assertThat(loaded.getBillingMonths()).isNull();
    }

    @Test
    void savingSetsIdAndAuditTimestamps() {
        Subscription s = trialFor(anita);
        assertThat(s.getId()).isNotNull();
        assertThat(s.getCreatedAt()).isNotNull();
    }

    // ---- findByUserId

    @Test
    void findByUserIdReturnsThatUsersSubscription() {
        Subscription saved = trialFor(anita);
        em.clear();
        assertThat(subscriptions.findByUserId(anita.getId()))
                .get().extracting(Subscription::getId).isEqualTo(saved.getId());
    }

    @Test
    void findByUserIdIsEmptyWhenTheUserHasNoSubscription() {
        trialFor(anita);
        assertThat(subscriptions.findByUserId(bongani.getId())).isEmpty();
    }

    @Test
    void findByUserIdIsEmptyForAnUnknownUser() {
        assertThat(subscriptions.findByUserId(999_999L)).isEmpty();
    }

    @Test
    void eachUserGetsTheirOwnSubscription() {
        Subscription forAnita = trialFor(anita);
        Subscription forBongani = trialFor(bongani);
        forBongani.setPlan(PlanType.FREE);
        forBongani.setStatus(SubscriptionStatus.EXPIRED);
        em.flush();
        em.clear();

        Subscription a = subscriptions.findByUserId(anita.getId()).orElseThrow();
        Subscription b = subscriptions.findByUserId(bongani.getId()).orElseThrow();
        assertThat(a.getId()).isEqualTo(forAnita.getId());
        assertThat(a.getPlan()).isEqualTo(PlanType.PRO);
        assertThat(b.getId()).isEqualTo(forBongani.getId());
        assertThat(b.getPlan()).isEqualTo(PlanType.FREE);
        assertThat(b.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    // ---- one subscription per user

    @Test
    void aSecondSubscriptionForTheSameUserIsRejected() {
        trialFor(anita);
        assertRejected(() -> trialFor(anita));
    }

    @Test
    void aSecondSubscriptionIsRejectedEvenWithADifferentPlanAndStatus() {
        trialFor(anita);
        Subscription second = new Subscription();
        second.setUser(anita);
        second.setPlan(PlanType.FREE);
        second.setStatus(SubscriptionStatus.ACTIVE);
        second.setTrialEndsAt(TRIAL_END);
        assertRejected(() -> em.persistAndFlush(second));
    }

    @Test
    void differentUsersCanEachHaveASubscription() {
        assertThat(trialFor(anita).getId()).isNotNull();
        assertThat(trialFor(bongani).getId()).isNotNull();
        assertThat(subscriptions.count()).isEqualTo(2);
    }

    @Test
    void userIdColumnIsUniqueInTheDatabase() {
        trialFor(anita);
        // the constraint is in the table itself, not only in Java code
        Integer uniqueConstraints = jdbc.queryForObject(
                "select count(*) from information_schema.table_constraints "
                        + "where lower(table_name) = 'subscriptions' and constraint_type = 'UNIQUE'",
                Integer.class);
        assertThat(uniqueConstraints).isGreaterThanOrEqualTo(1);
    }

    // ---- required values

    @Test
    void subscriptionWithoutAUserIsRejected() {
        Subscription s = new Subscription();
        s.setTrialEndsAt(TRIAL_END);
        assertRejected(() -> em.persistAndFlush(s));
    }

    @Test
    void subscriptionWithoutATrialEndDateIsRejected() {
        Subscription s = new Subscription();
        s.setUser(anita);
        assertRejected(() -> em.persistAndFlush(s));
    }

    // ---- lifecycle: trial, free plan, paid

    @Test
    void trialThatEndsBecomesTheFreePlan() {
        Subscription s = trialFor(anita);
        s.setPlan(PlanType.FREE);
        s.setStatus(SubscriptionStatus.EXPIRED);
        em.flush();
        em.clear();

        Subscription loaded = subscriptions.findByUserId(anita.getId()).orElseThrow();
        assertThat(loaded.getPlan()).isEqualTo(PlanType.FREE);
        assertThat(loaded.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void payingStoresThePeriodEndAndBillingMonths() {
        LocalDateTime periodEnd = LocalDateTime.of(2027, 3, 25, 9, 0);
        Subscription s = trialFor(anita);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setCurrentPeriodEnd(periodEnd);
        s.setBillingMonths(3);
        em.flush();
        em.clear();

        Subscription loaded = subscriptions.findByUserId(anita.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(loaded.getCurrentPeriodEnd()).isEqualTo(periodEnd);
        assertThat(loaded.getBillingMonths()).isEqualTo(3);
    }

    @Test
    void everyStatusCanBeStored() {
        Subscription s = trialFor(anita);
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            s.setStatus(status);
            em.flush();
            em.clear();
            assertThat(subscriptions.findByUserId(anita.getId()).orElseThrow().getStatus()).isEqualTo(status);
            s = subscriptions.findByUserId(anita.getId()).orElseThrow();
        }
    }

    @Test
    void planAndStatusAreStoredAsText() {
        Subscription s = trialFor(anita);
        String plan = jdbc.queryForObject("select plan from subscriptions where id = ?", String.class, s.getId());
        String status = jdbc.queryForObject("select status from subscriptions where id = ?", String.class, s.getId());
        assertThat(plan).isEqualTo("PRO");
        assertThat(status).isEqualTo("TRIAL");
    }

    @Test
    void deletingASubscriptionLetsTheUserHaveANewOne() {
        Subscription s = trialFor(anita);
        subscriptions.delete(s);
        em.flush();
        assertThat(subscriptions.findByUserId(anita.getId())).isEmpty();
        assertThat(trialFor(anita).getId()).isNotNull();
    }
}
