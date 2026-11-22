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
class StockMovementRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired StockMovementRepository movements;

    InventoryTestData data;
    Product milk, bread, otherShopProduct;
    static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 1, 8, 0);

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
        milk = data.product(1L, "Milk", "1", null, 0, 5);
        bread = data.product(1L, "Bread", "2", null, 0, 5);
        otherShopProduct = data.product(2L, "Other", "3", null, 0, 5);
    }

    @Test
    void productHistoryIsNewestFirst() {
        data.movement(milk.getId(), 10, StockMovementType.IN, 7L, T0);
        data.movement(milk.getId(), 3, StockMovementType.OUT, 7L, T0.plusHours(5));
        data.movement(milk.getId(), 20, StockMovementType.IN, 8L, T0.plusDays(1));
        data.movement(bread.getId(), 99, StockMovementType.IN, 7L, T0);
        assertThat(movements.findByProductIdOrderByScannedAtDesc(milk.getId()))
                .extracting(StockMovement::getQuantity).containsExactly(20, 3, 10);
    }

    @Test
    void historyKeepsWhoScannedAndWhen() {
        data.movement(milk.getId(), 10, StockMovementType.IN, 7L, T0);
        em.clear();
        StockMovement m = movements.findByProductIdOrderByScannedAtDesc(milk.getId()).get(0);
        assertThat(m.getScannedBy()).isEqualTo(7L);
        assertThat(m.getScannedAt()).isEqualTo(T0);
        assertThat(m.getType()).isEqualTo(StockMovementType.IN);
    }

    @Test
    void scannedAtDefaultsToNowWhenNotGiven() {
        StockMovement m = data.movement(milk.getId(), 1, StockMovementType.IN, 7L, null);
        assertThat(m.getScannedAt()).isNotNull();
    }

    @Test
    void businessFeedCoversOnlyThatBusinessesProductsNewestFirst() {
        data.movement(milk.getId(), 1, StockMovementType.IN, 7L, T0);
        data.movement(bread.getId(), 2, StockMovementType.IN, 7L, T0.plusHours(1));
        data.movement(otherShopProduct.getId(), 3, StockMovementType.IN, 7L, T0.plusHours(2));
        Page<StockMovement> page = movements.findByBusinessId(1L, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(StockMovement::getQuantity).containsExactly(2, 1);
    }

    @Test
    void businessFeedIsPaged() {
        for (int i = 0; i < 5; i++) data.movement(milk.getId(), i + 1, StockMovementType.IN, 7L, T0.plusMinutes(i));
        Page<StockMovement> page = movements.findByBusinessId(1L, PageRequest.of(1, 2));
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void netQuantityIsStockInMinusStockOut() {
        data.movement(milk.getId(), 10, StockMovementType.IN, 7L, T0);
        data.movement(milk.getId(), 4, StockMovementType.OUT, 7L, T0.plusHours(1));
        data.movement(milk.getId(), 5, StockMovementType.IN, 7L, T0.plusHours(2));
        assertThat(movements.netQuantityByProductId(milk.getId())).isEqualTo(11L);
    }

    @Test
    void netQuantityCanBeNegativeAndIsZeroWithNoMovements() {
        data.movement(bread.getId(), 3, StockMovementType.OUT, 7L, T0);
        assertThat(movements.netQuantityByProductId(bread.getId())).isEqualTo(-3L);
        assertThat(movements.netQuantityByProductId(milk.getId())).isZero();
    }

    @Test
    void movementWithoutAScannerIsRejected() {
        assertRejected(() -> data.movement(milk.getId(), 1, StockMovementType.IN, null, T0));
    }
}
