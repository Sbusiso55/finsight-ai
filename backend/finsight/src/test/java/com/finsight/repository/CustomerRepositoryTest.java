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
class CustomerRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired CustomerRepository customers;

    @Test
    void listIsAlphabeticalAndScopedToTheBusiness() {
        TestData data = new TestData(em);
        User u = data.user("u@example.com");
        Business shop = data.business(u, "Shop");
        Business other = data.business(u, "Other");
        data.customer(shop, "Zed");
        data.customer(shop, "Anita's Retail");
        data.customer(other, "Not mine");

        assertThat(customers.findByBusinessIdOrderByName(shop.getId())).extracting(Customer::getName)
                .containsExactly("Anita's Retail", "Zed");
    }

    @Test
    void nameLookupIgnoresCaseAndStaysInsideTheBusiness() {
        TestData data = new TestData(em);
        User u = data.user("u@example.com");
        Business shop = data.business(u, "Shop");
        Business other = data.business(u, "Other");
        data.customer(shop, "Anita's Retail");

        assertThat(customers.findByBusinessIdAndNameIgnoreCase(shop.getId(), "ANITA'S retail")).isPresent();
        assertThat(customers.findByBusinessIdAndNameIgnoreCase(other.getId(), "Anita's Retail")).isEmpty();
    }
}
