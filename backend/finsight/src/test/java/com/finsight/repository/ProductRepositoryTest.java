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
class ProductRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired ProductRepository products;

    InventoryTestData data;
    static final Long SHOP = 1L, OTHER = 2L;

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
    }

    @Test
    void newProductDefaultsToZeroStock() {
        Product p = new Product();
        assertThat(p.getQuantity()).isZero();
        assertThat(p.getMinStockLevel()).isZero();
    }

    @Test
    void scanningABarcodeFindsTheProductInThatBusinessOnly() {
        data.product(SHOP, "Milk", "600111", null, 10, 2);
        assertThat(products.findByBusinessIdAndBarcode(SHOP, "600111")).isPresent();
        assertThat(products.findByBusinessIdAndBarcode(OTHER, "600111")).isEmpty();
        assertThat(products.findByBusinessIdAndBarcode(SHOP, "999")).isEmpty();
    }

    @Test
    void scanningAQrCodeFindsTheProductInThatBusinessOnly() {
        data.product(SHOP, "Bread", null, "QR-1", 10, 2);
        assertThat(products.findByBusinessIdAndQrCode(SHOP, "QR-1")).isPresent();
        assertThat(products.findByBusinessIdAndQrCode(OTHER, "QR-1")).isEmpty();
    }

    @Test
    void existsChecks() {
        data.product(SHOP, "Milk", "600111", "QR-1", 10, 2);
        assertThat(products.existsByBusinessIdAndBarcode(SHOP, "600111")).isTrue();
        assertThat(products.existsByBusinessIdAndBarcode(OTHER, "600111")).isFalse();
        assertThat(products.existsByBusinessIdAndQrCode(SHOP, "QR-1")).isTrue();
        assertThat(products.existsByBusinessIdAndQrCode(OTHER, "QR-1")).isFalse();
    }

    @Test
    void sameBarcodeInTheSameBusinessIsRejected() {
        data.product(SHOP, "Milk", "600111", null, 10, 2);
        assertRejected(() -> data.product(SHOP, "Other milk", "600111", null, 1, 1));
    }

    @Test
    void sameQrCodeInTheSameBusinessIsRejected() {
        data.product(SHOP, "Bread", null, "QR-1", 10, 2);
        assertRejected(() -> data.product(SHOP, "Other bread", null, "QR-1", 1, 1));
    }

    @Test
    void sameBarcodeInDifferentBusinessesIsAllowed() {
        data.product(SHOP, "Milk", "600111", "QR-1", 10, 2);
        Product again = data.product(OTHER, "Milk", "600111", "QR-1", 10, 2);
        assertThat(again.getId()).isNotNull();
    }

    @Test
    void severalProductsWithoutABarcodeAreAllowed() {
        data.product(SHOP, "Loose sweets", null, null, 10, 2);
        Product second = data.product(SHOP, "Loose nuts", null, null, 10, 2);
        assertThat(second.getId()).isNotNull();
    }

    @Test
    void productWithoutANameIsRejected() {
        assertRejected(() -> data.product(SHOP, null, "600111", null, 1, 1));
    }

    @Test
    void listIsAlphabeticalAndScopedToTheBusiness() {
        data.product(SHOP, "Zebra cakes", "1", null, 5, 1);
        data.product(SHOP, "Apples", "2", null, 5, 1);
        data.product(OTHER, "Not mine", "3", null, 5, 1);
        assertThat(products.findByBusinessIdOrderByName(SHOP)).extracting(Product::getName)
                .containsExactly("Apples", "Zebra cakes");
    }

    @Test
    void findByIdAndBusinessIdHidesOtherBusinessesProducts() {
        Product p = data.product(SHOP, "Milk", "1", null, 5, 1);
        assertThat(products.findByIdAndBusinessId(p.getId(), SHOP)).isPresent();
        assertThat(products.findByIdAndBusinessId(p.getId(), OTHER)).isEmpty();
    }

    @Test
    void findBySupplierIdReturnsThatSuppliersProducts() {
        Supplier supplier = data.supplier(SHOP, "Fresh Farms");
        Product milk = data.product(SHOP, "Milk", "1", null, 5, 1);
        milk.setSupplierId(supplier.getId());
        em.persistAndFlush(milk);
        data.product(SHOP, "Bread", "2", null, 5, 1);
        assertThat(products.findBySupplierId(supplier.getId())).extracting(Product::getName).containsExactly("Milk");
    }

    @Test
    void lowStockIncludesBelowAndExactlyAtTheMinimumButNotAbove() {
        data.product(SHOP, "Below", "1", null, 1, 5);
        data.product(SHOP, "AtMinimum", "2", null, 5, 5);
        data.product(SHOP, "Above", "3", null, 6, 5);
        assertThat(products.findLowStock(SHOP)).extracting(Product::getName)
                .containsExactly("Below", "AtMinimum");
    }

    @Test
    void lowStockIsLowestQuantityFirstThenByName() {
        data.product(SHOP, "B item", "1", null, 3, 10);
        data.product(SHOP, "A item", "2", null, 3, 10);
        data.product(SHOP, "Empty", "3", null, 0, 10);
        assertThat(products.findLowStock(SHOP)).extracting(Product::getName)
                .containsExactly("Empty", "A item", "B item");
    }

    @Test
    void lowStockIsScopedToTheBusiness() {
        data.product(OTHER, "Other low", "1", null, 0, 5);
        assertThat(products.findLowStock(SHOP)).isEmpty();
    }

    @Test
    void outOfStockProductWithNoMinimumIsStillLow() {
        data.product(SHOP, "Gone", "1", null, 0, 0);   // 0 <= 0
        assertThat(products.findLowStock(SHOP)).hasSize(1);
    }
}
