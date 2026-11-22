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
class SupplierRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired SupplierRepository suppliers;

    @Test
    void listIsAlphabeticalAndScopedToTheBusiness() {
        InventoryTestData data = new InventoryTestData(em);
        data.supplier(1L, "Zulu Wholesale");
        data.supplier(1L, "Acme Foods");
        data.supplier(2L, "Not mine");
        assertThat(suppliers.findByBusinessIdOrderByName(1L)).extracting(Supplier::getName)
                .containsExactly("Acme Foods", "Zulu Wholesale");
    }

    @Test
    void findByIdAndBusinessIdHidesOtherBusinessesSuppliers() {
        Supplier s = new InventoryTestData(em).supplier(1L, "Acme Foods");
        assertThat(suppliers.findByIdAndBusinessId(s.getId(), 1L)).isPresent();
        assertThat(suppliers.findByIdAndBusinessId(s.getId(), 2L)).isEmpty();
    }

    @Test
    void phoneAndEmailAreOptional() {
        Supplier s = new InventoryTestData(em).supplier(1L, "Acme Foods");
        em.clear();
        Supplier loaded = suppliers.findById(s.getId()).orElseThrow();
        assertThat(loaded.getPhone()).isNull();
        assertThat(loaded.getEmail()).isNull();
    }

    @Test
    void supplierWithoutANameIsRejected() {
        assertRejected(() -> new InventoryTestData(em).supplier(1L, null));
    }
}
