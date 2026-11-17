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
class ExpenseInsightRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired ExpenseInsightRepository insights;

    TestData data;
    Business shop, other;
    Category salaries;
    static final LocalDate JAN = LocalDate.of(2026, 1, 1);
    static final LocalDate FEB = LocalDate.of(2026, 2, 1);

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");
        salaries = data.expense(shop, "Salaries", CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
    }

    @Test
    void findByBusinessAndMonthIsScopedAndLoadsTheCategory() {
        data.insight(shop, JAN, InsightType.LARGEST_EXPENSE, salaries, false);
        data.insight(shop, FEB, InsightType.HIGH_FIXED_COSTS, null, false);
        data.insight(other, JAN, InsightType.HIGH_FIXED_COSTS, null, false);
        em.clear();

        List<ExpenseInsight> result = insights.findByBusinessIdAndPeriodStart(shop.getId(), JAN);
        assertThat(result).hasSize(1);
        assertThat(Hibernate.isInitialized(result.get(0).getCategory())).isTrue();
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Salaries");
    }

    @Test
    void activeInsightsSkipDismissedOnesAndAreNewestMonthFirst() {
        data.insight(shop, JAN, InsightType.LARGEST_EXPENSE, salaries, false);
        data.insight(shop, FEB, InsightType.HIGH_FIXED_COSTS, null, false);
        data.insight(shop, FEB, InsightType.HIGH_EXPENSE_RATIO, null, true);   // dismissed
        em.clear();

        assertThat(insights.findByBusinessIdAndDismissedFalseOrderByPeriodStartDesc(shop.getId()))
                .extracting(ExpenseInsight::getType)
                .containsExactly(InsightType.HIGH_FIXED_COSTS, InsightType.LARGEST_EXPENSE);
    }

    @Test
    void deleteByBusinessAndPeriodRemovesOnlyThatMonthForThatBusiness() {
        data.insight(shop, JAN, InsightType.LARGEST_EXPENSE, salaries, false);
        data.insight(shop, JAN, InsightType.HIGH_FIXED_COSTS, null, false);
        data.insight(shop, FEB, InsightType.HIGH_FIXED_COSTS, null, false);
        data.insight(other, JAN, InsightType.HIGH_FIXED_COSTS, null, false);

        insights.deleteByBusinessAndPeriod(shop.getId(), JAN);
        em.clear();

        assertThat(insights.findByBusinessIdAndPeriodStart(shop.getId(), JAN)).isEmpty();
        assertThat(insights.findByBusinessIdAndPeriodStart(shop.getId(), FEB)).hasSize(1);
        assertThat(insights.findByBusinessIdAndPeriodStart(other.getId(), JAN)).hasSize(1);
    }
}
