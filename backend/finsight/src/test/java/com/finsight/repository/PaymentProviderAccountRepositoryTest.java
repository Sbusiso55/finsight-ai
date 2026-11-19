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
class PaymentProviderAccountRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired PaymentProviderAccountRepository accounts;

    InventoryTestData data;

    @BeforeEach
    void setUp() {
        data = new InventoryTestData(em);
    }

    @Test
    void findByBusinessAndProvider() {
        data.providerAccount(1L, PaymentProvider.YOCO, "enc-1");
        data.providerAccount(1L, PaymentProvider.PAYSTACK, "enc-2");
        data.providerAccount(2L, PaymentProvider.YOCO, "enc-3");
        assertThat(accounts.findByBusinessIdAndProvider(1L, PaymentProvider.PAYSTACK)).isPresent();
        assertThat(accounts.findByBusinessIdAndProvider(1L, PaymentProvider.ZAPPER)).isEmpty();
        assertThat(accounts.findByBusinessId(1L)).hasSize(2);
        assertThat(accounts.existsByBusinessIdAndProvider(2L, PaymentProvider.YOCO)).isTrue();
        assertThat(accounts.existsByBusinessIdAndProvider(2L, PaymentProvider.PEACH)).isFalse();
    }

    @Test
    void sameProviderCannotBeConnectedTwiceForOneBusiness() {
        data.providerAccount(1L, PaymentProvider.YOCO, "enc-1");
        assertRejected(() -> data.providerAccount(1L, PaymentProvider.YOCO, "enc-2"));
    }

    @Test
    void differentBusinessesCanConnectTheSameProvider() {
        data.providerAccount(1L, PaymentProvider.YOCO, "enc-1");
        assertThat(data.providerAccount(2L, PaymentProvider.YOCO, "enc-2").getId()).isNotNull();
    }

    @Test
    void connectedAtDefaultsToNow() {
        assertThat(data.providerAccount(1L, PaymentProvider.YOCO, "enc-1").getConnectedAt()).isNotNull();
    }

    @Test
    void keyIsRequired() {
        assertRejected(() -> data.providerAccount(1L, PaymentProvider.YOCO, null));
    }

    @Test
    void theStoredValueIsExactlyWhatWasGivenSoItMustAlreadyBeEncrypted() {
        PaymentProviderAccount a = data.providerAccount(1L, PaymentProvider.YOCO, "v1:ENCRYPTED-BLOB");
        em.clear();
        assertThat(accounts.findById(a.getId()).orElseThrow().getEncryptedApiKey()).isEqualTo("v1:ENCRYPTED-BLOB");
    }
}
