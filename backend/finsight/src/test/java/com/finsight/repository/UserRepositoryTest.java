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
class UserRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired UserRepository users;

    @Test
    void findByEmailReturnsTheUser() {
        new TestData(em).user("a@example.com");
        assertThat(users.findByEmail("a@example.com")).isPresent();
    }

    @Test
    void findByEmailIsEmptyForUnknownEmail() {
        assertThat(users.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void existsByEmail() {
        new TestData(em).user("a@example.com");
        assertThat(users.existsByEmail("a@example.com")).isTrue();
        assertThat(users.existsByEmail("b@example.com")).isFalse();
    }
}
