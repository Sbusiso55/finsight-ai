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
class AuditLogRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired AuditLogRepository logs;

    InventoryTestData data;
    static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 1, 9, 0);

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
    }

    @Test
    void userHistoryIsNewestFirstAndScopedToTheUser() {
        data.audit(7L, "CREATE", "Product", 1L, T0);
        data.audit(7L, "UPDATE", "Product", 1L, T0.plusHours(1));
        data.audit(8L, "DELETE", "Product", 2L, T0.plusHours(2));
        Page<AuditLog> page = logs.findByUserIdOrderByCreatedAtDesc(7L, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(AuditLog::getAction).containsExactly("UPDATE", "CREATE");
    }

    @Test
    void recordHistoryMatchesTypeAndIdNewestFirst() {
        data.audit(7L, "CREATE", "Product", 1L, T0);
        data.audit(8L, "UPDATE", "Product", 1L, T0.plusHours(1));
        data.audit(7L, "UPDATE", "Product", 2L, T0.plusHours(2));     // different id
        data.audit(7L, "UPDATE", "Supplier", 1L, T0.plusHours(3));    // different type
        Page<AuditLog> page = logs.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Product", 1L, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(AuditLog::getAction).containsExactly("UPDATE", "CREATE");
    }

    @Test
    void createdAtDefaultsToNowAndEntityIdIsOptional() {
        AuditLog l = data.audit(7L, "LOGIN", "User", null, null);
        assertThat(l.getCreatedAt()).isNotNull();
        assertThat(l.getEntityId()).isNull();
    }

    @Test
    void actionAndUserAreRequired() {
        assertRejected(() -> data.audit(7L, null, "Product", 1L, T0));
        assertRejected(() -> data.audit(null, "CREATE", "Product", 1L, T0));
    }
}
