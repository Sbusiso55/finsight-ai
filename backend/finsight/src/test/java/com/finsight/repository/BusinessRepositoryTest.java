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
class BusinessRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired BusinessRepository businesses;

    TestData data;
    User anita, bongani, thandi;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        anita = data.user("anita@example.com");
        bongani = data.user("bongani@example.com");
        thandi = data.user("thandi@example.com");
    }

    @Test
    void findByOwnerIdReturnsOnlyThatOwnersBusinesses() {
        data.business(anita, "Anita 1");
        data.business(anita, "Anita 2");
        data.business(bongani, "Bongani 1");
        assertThat(businesses.findByOwnerId(anita.getId())).extracting(Business::getName)
                .containsExactlyInAnyOrder("Anita 1", "Anita 2");
    }

    @Test
    void accessibleIncludesOwnedBusinesses() {
        data.business(anita, "Owned");
        assertThat(businesses.findAccessibleByUser(anita.getId())).extracting(Business::getName)
                .containsExactly("Owned");
    }

    @Test
    void accessibleIncludesBusinessesWhereUserIsAMember() {
        Business others = data.business(bongani, "Bongani Shop");
        data.member(others, anita, Role.ACCOUNTANT);
        assertThat(businesses.findAccessibleByUser(anita.getId())).extracting(Business::getName)
                .containsExactly("Bongani Shop");
    }

    @Test
    void accessibleExcludesBusinessesTheUserHasNothingToDoWith() {
        data.business(bongani, "Private");
        assertThat(businesses.findAccessibleByUser(anita.getId())).isEmpty();
    }

    @Test
    void ownerWhoIsAlsoAMemberIsReturnedOnlyOnce() {
        Business b = data.business(anita, "Both");
        data.member(b, anita, Role.OWNER);
        assertThat(businesses.findAccessibleByUser(anita.getId())).hasSize(1);
    }

    @Test
    void accessibleIsSortedByName() {
        data.business(anita, "Zulu");
        Business alpha = data.business(bongani, "Alpha");
        data.member(alpha, anita, Role.EMPLOYEE);
        data.business(anita, "Mike");
        assertThat(businesses.findAccessibleByUser(anita.getId())).extracting(Business::getName)
                .containsExactly("Alpha", "Mike", "Zulu");
    }
}
