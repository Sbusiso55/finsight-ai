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
class StockAlertRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired StockAlertRepository alerts;

    InventoryTestData data;
    Product milk, bread, otherShopProduct;
    static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 1, 8, 0);

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
        milk = data.product(1L, "Milk", "1", null, 2, 5);
        bread = data.product(1L, "Bread", "2", null, 2, 5);
        otherShopProduct = data.product(2L, "Other", "3", null, 2, 5);
    }

    @Test
    void newAlertStartsOpenAndGetsATimestamp() {
        StockAlert a = new StockAlert();
        a.setProductId(milk.getId());
        a.setCurrentQuantity(2);
        a.setMinStockLevel(5);
        em.persistAndFlush(a);
        assertThat(a.getStatus()).isEqualTo(StockAlertStatus.OPEN);
        assertThat(a.getCreatedAt()).isNotNull();
    }

    @Test
    void findByProductAndStatus() {
        data.alert(milk.getId(), StockAlertStatus.OPEN, T0);
        data.alert(milk.getId(), StockAlertStatus.RESOLVED, T0.plusDays(1));
        assertThat(alerts.findByProductIdAndStatus(milk.getId(), StockAlertStatus.OPEN)).hasSize(1);
        assertThat(alerts.findByProductIdAndStatus(milk.getId(), StockAlertStatus.RESOLVED)).hasSize(1);
        assertThat(alerts.findByProductIdAndStatus(bread.getId(), StockAlertStatus.OPEN)).isEmpty();
    }

    @Test
    void existsOpenAlertIgnoresResolvedOnes() {
        data.alert(milk.getId(), StockAlertStatus.RESOLVED, T0);
        assertThat(alerts.existsByProductIdAndStatus(milk.getId(), StockAlertStatus.OPEN)).isFalse();
        data.alert(milk.getId(), StockAlertStatus.OPEN, T0.plusDays(1));
        assertThat(alerts.existsByProductIdAndStatus(milk.getId(), StockAlertStatus.OPEN)).isTrue();
    }

    @Test
    void businessAlertsAreFilteredByStatusScopedAndNewestFirst() {
        data.alert(milk.getId(), StockAlertStatus.OPEN, T0);
        data.alert(bread.getId(), StockAlertStatus.OPEN, T0.plusDays(1));
        data.alert(bread.getId(), StockAlertStatus.RESOLVED, T0.plusDays(2));
        data.alert(otherShopProduct.getId(), StockAlertStatus.OPEN, T0.plusDays(3));

        List<StockAlert> open = alerts.findByBusinessIdAndStatus(1L, StockAlertStatus.OPEN);
        assertThat(open).extracting(StockAlert::getProductId).containsExactly(bread.getId(), milk.getId());
    }

    @Test
    void alertKeepsTheQuantitiesFromWhenItWasRaised() {
        data.alert(milk.getId(), StockAlertStatus.OPEN, T0);
        em.clear();
        StockAlert a = alerts.findByProductIdAndStatus(milk.getId(), StockAlertStatus.OPEN).get(0);
        assertThat(a.getCurrentQuantity()).isEqualTo(2);
        assertThat(a.getMinStockLevel()).isEqualTo(5);
    }
}
