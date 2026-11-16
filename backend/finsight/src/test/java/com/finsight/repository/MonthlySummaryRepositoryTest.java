package com.finsight.repository;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import com.finsight.testsupport.JpaTest;
import com.finsight.testsupport.TestData;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class MonthlySummaryRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired MonthlySummaryRepository summaries;

    TestData data;
    Business shop, other;

    static LocalDate month(int m) { return LocalDate.of(2026, m, 1); }

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");
        for (int m : new int[]{3, 1, 5, 2, 4}) data.summary(shop, month(m));   // inserted out of order on purpose
        data.summary(other, month(1));
    }

    @Test
    void findByBusinessIdAndPeriodStart() {
        assertThat(summaries.findByBusinessIdAndPeriodStart(shop.getId(), month(3))).isPresent();
        assertThat(summaries.findByBusinessIdAndPeriodStart(shop.getId(), month(9))).isEmpty();
    }

    @Test
    void historyIsOldestFirstAndScoped() {
        assertThat(summaries.findByBusinessIdOrderByPeriodStartAsc(shop.getId()))
                .extracting(MonthlySummary::getPeriodStart)
                .containsExactly(month(1), month(2), month(3), month(4), month(5));
    }

    @Test
    void betweenIsInclusiveOnBothEnds() {
        assertThat(summaries.findByBusinessIdAndPeriodStartBetweenOrderByPeriodStartAsc(
                shop.getId(), month(2), month(4)))
                .extracting(MonthlySummary::getPeriodStart)
                .containsExactly(month(2), month(3), month(4));
    }

    @Test
    void lastThreeMonthsUpToADateAreNewestFirst() {
        List<MonthlySummary> last3 = summaries.findByBusinessIdAndPeriodStartLessThanEqualOrderByPeriodStartDesc(
                shop.getId(), month(4), PageRequest.of(0, 3));
        assertThat(last3).extracting(MonthlySummary::getPeriodStart)
                .containsExactly(month(4), month(3), month(2));
    }

    @Test
    void countByBusinessIdOnlyCountsThatBusiness() {
        assertThat(summaries.countByBusinessId(shop.getId())).isEqualTo(5);
        assertThat(summaries.countByBusinessId(other.getId())).isEqualTo(1);
    }
}
