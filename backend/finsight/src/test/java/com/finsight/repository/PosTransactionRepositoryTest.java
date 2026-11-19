package com.finsight.repository;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import com.finsight.testsupport.InventoryTestData;
import com.finsight.testsupport.JpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.finsight.testsupport.InventoryTestData.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class PosTransactionRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired PosTransactionRepository payments;

    InventoryTestData data;
    static final Long SHOP = 1L, OTHER = 2L;
    static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 1, 9, 0);

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
    }

    // ---- uniqueness: never save the same payment twice

    @Test
    void samePaymentCannotBeSavedTwice() {
        data.payment(SHOP, "yoco_abc", "100.00", PosTransactionStatus.SUCCESS, T0);
        assertRejected(() -> data.payment(SHOP, "yoco_abc", "100.00", PosTransactionStatus.SUCCESS, T0));
    }

    @Test
    void duplicateIsRejectedEvenForADifferentBusiness() {
        data.payment(SHOP, "yoco_abc", "100.00", PosTransactionStatus.SUCCESS, T0);
        assertRejected(() -> data.payment(OTHER, "yoco_abc", "100.00", PosTransactionStatus.SUCCESS, T0));
    }

    @Test
    void differentTransactionIdsAreBothSaved() {
        data.payment(SHOP, "yoco_1", "10.00", PosTransactionStatus.SUCCESS, T0);
        PosTransaction second = data.payment(SHOP, "yoco_2", "10.00", PosTransactionStatus.SUCCESS, T0);
        assertThat(second.getId()).isNotNull();
    }

    @Test
    void paymentWithoutAProviderTransactionIdIsRejected() {
        assertRejected(() -> data.payment(SHOP, null, "10.00", PosTransactionStatus.SUCCESS, T0));
    }

    @Test
    void duplicateCheckAndLookup() {
        data.payment(SHOP, "yoco_abc", "100.00", PosTransactionStatus.SUCCESS, T0);
        assertThat(payments.existsByProviderTransactionId("yoco_abc")).isTrue();
        assertThat(payments.existsByProviderTransactionId("yoco_zzz")).isFalse();
        assertThat(payments.findByProviderTransactionId("yoco_abc")).isPresent();
        assertThat(payments.findByProviderTransactionId("yoco_zzz")).isEmpty();
    }

    // ---- defaults

    @Test
    void newPaymentIsPendingAndGetsATimestamp() {
        PosTransaction t = new PosTransaction();
        t.setBusinessId(SHOP);
        t.setProvider(PaymentProvider.PEACH);
        t.setProviderTransactionId("peach_1");
        t.setAmount(new BigDecimal("5.00"));
        t.setPaymentMethod(PaymentMethod.QR);
        em.persistAndFlush(t);
        assertThat(t.getStatus()).isEqualTo(PosTransactionStatus.PENDING);
        assertThat(t.getCreatedAt()).isNotNull();
    }

    // ---- total paid so far

    @Test
    void totalPaidCountsOnlySuccessfulPayments() {
        data.payment(SHOP, "a", "100.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(SHOP, "b", "250.50", PosTransactionStatus.SUCCESS, T0.plusMinutes(5));
        data.payment(SHOP, "c", "999.00", PosTransactionStatus.PENDING, T0.plusMinutes(6));
        data.payment(SHOP, "d", "888.00", PosTransactionStatus.FAILED, T0.plusMinutes(7));
        data.payment(SHOP, "e", "777.00", PosTransactionStatus.REFUNDED, T0.plusMinutes(8));
        assertThat(payments.totalPaid(SHOP)).isEqualByComparingTo("350.50");
    }

    @Test
    void totalPaidIsScopedToTheBusiness() {
        data.payment(SHOP, "a", "100.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(OTHER, "b", "5000.00", PosTransactionStatus.SUCCESS, T0);
        assertThat(payments.totalPaid(SHOP)).isEqualByComparingTo("100.00");
    }

    @Test
    void totalPaidIsZeroNotNullWhenThereAreNoPayments() {
        assertThat(payments.totalPaid(SHOP)).isNotNull().isEqualByComparingTo("0");
    }

    @Test
    void totalPaidBetweenIsInclusiveAndSuccessOnly() {
        data.payment(SHOP, "before", "1.00", PosTransactionStatus.SUCCESS, T0.minusSeconds(1));
        data.payment(SHOP, "first", "10.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(SHOP, "last", "20.00", PosTransactionStatus.SUCCESS, T0.plusHours(1));
        data.payment(SHOP, "failed", "500.00", PosTransactionStatus.FAILED, T0.plusMinutes(30));
        data.payment(SHOP, "after", "2.00", PosTransactionStatus.SUCCESS, T0.plusHours(1).plusSeconds(1));
        assertThat(payments.totalPaidBetween(SHOP, T0, T0.plusHours(1))).isEqualByComparingTo("30.00");
    }

    @Test
    void totalPaidBetweenIsZeroWhenNothingMatches() {
        assertThat(payments.totalPaidBetween(SHOP, T0, T0.plusDays(1))).isEqualByComparingTo("0");
    }

    @Test
    void amountsKeepTwoDecimalPlaces() {
        data.payment(SHOP, "a", "1234.56", PosTransactionStatus.SUCCESS, T0);
        em.clear();
        assertThat(payments.findByProviderTransactionId("a").orElseThrow().getAmount()).isEqualByComparingTo("1234.56");
    }

    // ---- feed and counts

    @Test
    void feedIsNewestFirstAndScopedToTheBusiness() {
        data.payment(SHOP, "old", "1.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(SHOP, "new", "2.00", PosTransactionStatus.SUCCESS, T0.plusHours(2));
        data.payment(SHOP, "mid", "3.00", PosTransactionStatus.SUCCESS, T0.plusHours(1));
        data.payment(OTHER, "not-mine", "4.00", PosTransactionStatus.SUCCESS, T0.plusHours(3));
        Page<PosTransaction> page = payments.findByBusinessIdOrderByCreatedAtDesc(SHOP, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(PosTransaction::getProviderTransactionId)
                .containsExactly("new", "mid", "old");
    }

    @Test
    void feedIsPaged() {
        for (int i = 0; i < 5; i++) data.payment(SHOP, "p" + i, "1.00", PosTransactionStatus.SUCCESS, T0.plusMinutes(i));
        Page<PosTransaction> page = payments.findByBusinessIdOrderByCreatedAtDesc(SHOP, PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void countByStatus() {
        data.payment(SHOP, "a", "1.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(SHOP, "b", "1.00", PosTransactionStatus.SUCCESS, T0);
        data.payment(SHOP, "c", "1.00", PosTransactionStatus.FAILED, T0);
        assertThat(payments.countByBusinessIdAndStatus(SHOP, PosTransactionStatus.SUCCESS)).isEqualTo(2);
        assertThat(payments.countByBusinessIdAndStatus(SHOP, PosTransactionStatus.FAILED)).isEqualTo(1);
        assertThat(payments.countByBusinessIdAndStatus(OTHER, PosTransactionStatus.SUCCESS)).isZero();
    }
}
