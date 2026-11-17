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
class BusinessMemberRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired BusinessMemberRepository members;

    TestData data;
    User owner, accountant;
    Business shop, cafe;

    @BeforeEach
    void setUp() {
        data = new TestData(em);
        owner = data.user("owner@example.com");
        accountant = data.user("acc@example.com");
        shop = data.business(owner, "Shop");
        cafe = data.business(owner, "Cafe");
        data.member(shop, owner, Role.OWNER);
        data.member(shop, accountant, Role.ACCOUNTANT);
        data.member(cafe, owner, Role.OWNER);
        em.clear();
    }

    @Test
    void findByBusinessIdReturnsEveryMemberAndLoadsTheUser() {
        List<BusinessMember> result = members.findByBusinessId(shop.getId());
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(m -> assertThat(Hibernate.isInitialized(m.getUser())).isTrue());
    }

    @Test
    void findByUserIdReturnsTheirMembershipsAndLoadsTheBusiness() {
        List<BusinessMember> result = members.findByUserId(owner.getId());
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(m -> assertThat(Hibernate.isInitialized(m.getBusiness())).isTrue());
    }

    @Test
    void findByBusinessIdAndUserIdGivesTheRole() {
        assertThat(members.findByBusinessIdAndUserId(shop.getId(), accountant.getId()))
                .get().extracting(BusinessMember::getRole).isEqualTo(Role.ACCOUNTANT);
    }

    @Test
    void findByBusinessIdAndUserIdIsEmptyForNonMember() {
        assertThat(members.findByBusinessIdAndUserId(cafe.getId(), accountant.getId())).isEmpty();
    }

    @Test
    void existsByBusinessIdAndUserId() {
        assertThat(members.existsByBusinessIdAndUserId(shop.getId(), accountant.getId())).isTrue();
        assertThat(members.existsByBusinessIdAndUserId(cafe.getId(), accountant.getId())).isFalse();
    }
}
