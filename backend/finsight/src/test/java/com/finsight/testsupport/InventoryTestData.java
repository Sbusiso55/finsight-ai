package com.finsight.testsupport;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Builds and saves valid inventory / POS rows so each test only spells out what it cares about. */
public class InventoryTestData {

    private final TestEntityManager em;

    public InventoryTestData(TestEntityManager em) {
        this.em = em;
    }

    /** Asserts that the database refuses the action (constraint violation, missing required value...). */
    public static void assertRejected(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfAny(PersistenceException.class, HibernateException.class, DataAccessException.class);
    }

    public Product product(Long businessId, String name, String barcode, String qrCode, int quantity, int min) {
        Product p = new Product();
        p.setBusinessId(businessId);
        p.setName(name);
        p.setBarcode(barcode);
        p.setQrCode(qrCode);
        p.setQuantity(quantity);
        p.setMinStockLevel(min);
        p.setUnitPrice(new BigDecimal("10.00"));
        return em.persistAndFlush(p);
    }

    public Supplier supplier(Long businessId, String name) {
        Supplier s = new Supplier();
        s.setBusinessId(businessId);
        s.setName(name);
        return em.persistAndFlush(s);
    }

    public StockMovement movement(Long productId, int quantity, StockMovementType type, Long scannedBy, LocalDateTime at) {
        StockMovement m = new StockMovement();
        m.setProductId(productId);
        m.setQuantity(quantity);
        m.setType(type);
        m.setScannedBy(scannedBy);
        m.setScannedAt(at);
        return em.persistAndFlush(m);
    }

    public StockAlert alert(Long productId, StockAlertStatus status, LocalDateTime at) {
        StockAlert a = new StockAlert();
        a.setProductId(productId);
        a.setCurrentQuantity(2);
        a.setMinStockLevel(5);
        a.setStatus(status);
        a.setCreatedAt(at);
        return em.persistAndFlush(a);
    }

    public PosTransaction payment(Long businessId, String providerTxnId, String amount,
                                  PosTransactionStatus status, LocalDateTime at) {
        return payment(PaymentProvider.YOCO, businessId, providerTxnId, amount, status, at);
    }

    public PosTransaction payment(PaymentProvider provider, Long businessId, String providerTxnId, String amount,
                                  PosTransactionStatus status, LocalDateTime at) {
        PosTransaction t = new PosTransaction();
        t.setBusinessId(businessId);
        t.setProvider(provider);
        t.setProviderTransactionId(providerTxnId);
        t.setAmount(new BigDecimal(amount));
        t.setPaymentMethod(PaymentMethod.TAP);
        t.setStatus(status);
        t.setCreatedAt(at);
        return em.persistAndFlush(t);
    }

    public PaymentProviderAccount providerAccount(Long businessId, PaymentProvider provider, String encryptedKey) {
        PaymentProviderAccount a = new PaymentProviderAccount();
        a.setBusinessId(businessId);
        a.setProvider(provider);
        a.setEncryptedApiKey(encryptedKey);
        return em.persistAndFlush(a);
    }

    public AuditLog audit(Long userId, String action, String entityType, Long entityId, LocalDateTime at) {
        AuditLog l = new AuditLog();
        l.setUserId(userId);
        l.setAction(action);
        l.setEntityType(entityType);
        l.setEntityId(entityId);
        l.setCreatedAt(at);
        return em.persistAndFlush(l);
    }
}
