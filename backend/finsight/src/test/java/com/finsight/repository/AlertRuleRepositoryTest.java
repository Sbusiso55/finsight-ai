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
class AlertRuleRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired AlertRuleRepository rules;

    TestData data;
    Business shop, other;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");
        data.rule(shop, InsightType.LARGEST_EXPENSE, true);
        data.rule(shop, InsightType.HIGH_FIXED_COSTS, false);
        data.rule(other, InsightType.LARGEST_EXPENSE, true);
    }

    @Test
    void findByBusinessIdReturnsAllRulesForThatBusiness() {
        assertThat(rules.findByBusinessId(shop.getId())).hasSize(2);
    }

    @Test
    void enabledOnlyFiltersOutDisabledRules() {
        assertThat(rules.findByBusinessIdAndEnabledTrue(shop.getId())).extracting(AlertRule::getRuleType)
                .containsExactly(InsightType.LARGEST_EXPENSE);
    }

    @Test
    void findByRuleTypeIsScopedToTheBusiness() {
        assertThat(rules.findByBusinessIdAndRuleType(shop.getId(), InsightType.HIGH_FIXED_COSTS)).isPresent();
        assertThat(rules.findByBusinessIdAndRuleType(other.getId(), InsightType.HIGH_FIXED_COSTS)).isEmpty();
    }
}
