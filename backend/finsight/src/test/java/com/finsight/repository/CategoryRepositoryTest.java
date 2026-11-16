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
class CategoryRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired CategoryRepository categories;

    TestData data;
    Business shop, other;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        User u = data.user("u@example.com");
        shop = data.business(u, "Shop");
        other = data.business(u, "Other");
        data.income(shop, "Sales");
        data.expense(shop, "Rent", CostBehavior.FIXED, CostClassification.OPERATING_EXPENSE);
        data.expense(shop, "Advertising", CostBehavior.VARIABLE, CostClassification.OPERATING_EXPENSE);
        data.income(other, "Other business sales");
    }

    @Test
    void findByBusinessIdOrderByNameIsAlphabeticalAndScoped() {
        assertThat(categories.findByBusinessIdOrderByName(shop.getId())).extracting(Category::getName)
                .containsExactly("Advertising", "Rent", "Sales");
    }

    @Test
    void findByBusinessIdAndTypeFiltersByType() {
        assertThat(categories.findByBusinessIdAndType(shop.getId(), TransactionType.EXPENSE))
                .extracting(Category::getName).containsExactlyInAnyOrder("Rent", "Advertising");
        assertThat(categories.findByBusinessIdAndType(shop.getId(), TransactionType.INCOME))
                .extracting(Category::getName).containsExactly("Sales");
    }

    @Test
    void nameLookupIgnoresCase() {
        assertThat(categories.findByBusinessIdAndNameIgnoreCase(shop.getId(), "sALes")).isPresent();
        assertThat(categories.existsByBusinessIdAndNameIgnoreCase(shop.getId(), "RENT")).isTrue();
    }

    @Test
    void nameLookupDoesNotCrossBusinesses() {
        assertThat(categories.findByBusinessIdAndNameIgnoreCase(other.getId(), "Rent")).isEmpty();
        assertThat(categories.existsByBusinessIdAndNameIgnoreCase(other.getId(), "Rent")).isFalse();
    }
}
