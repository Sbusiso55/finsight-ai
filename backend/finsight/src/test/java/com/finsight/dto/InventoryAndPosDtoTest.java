package com.finsight.dto;

import com.finsight.model.*;
import com.finsight.model.enums.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests (no database): the DTOs copy the right fields and never expose anything sensitive. */
class InventoryAndPosDtoTest {

    static final LocalDateTime AT = LocalDateTime.of(2026, 3, 1, 9, 0);

    private static List<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(Field::getName).toList();
    }

    @Test
    void noDtoEverContainsTheApiKey() {
        for (Class<?> dto : List.of(ProductDTO.class, SupplierDTO.class, StockMovementDTO.class,
                StockAlertDTO.class, PosTransactionDTO.class, PosTotalsDTO.class,
                PaymentProviderAccountDTO.class, AuditLogDTO.class)) {
            assertThat(fieldNames(dto)).as(dto.getSimpleName())
                    .noneMatch(n -> n.toLowerCase().contains("apikey") || n.toLowerCase().contains("secret"));
        }
    }

    @Test
    void providerAccountDtoShowsOnlyIdProviderAndConnectedAt() {
        PaymentProviderAccount a = new PaymentProviderAccount();
        a.setId(5L);
        a.setBusinessId(1L);
        a.setProvider(PaymentProvider.YOCO);
        a.setEncryptedApiKey("v1:SUPER-SECRET");
        a.setConnectedAt(AT);

        PaymentProviderAccountDTO dto = PaymentProviderAccountDTO.from(a);
        assertThat(fieldNames(PaymentProviderAccountDTO.class)).containsExactlyInAnyOrder("id", "provider", "connectedAt");
        assertThat(dto.getProvider()).isEqualTo(PaymentProvider.YOCO);
        assertThat(dto.getConnectedAt()).isEqualTo(AT);
        assertThat(dto.toString()).doesNotContain("SUPER-SECRET");
    }

    @Test
    void posTransactionDtoLeavesOutBusinessIdAndProviderReference() {
        assertThat(fieldNames(PosTransactionDTO.class))
                .containsExactlyInAnyOrder("id", "provider", "amount", "paymentMethod", "status", "createdAt");
    }

    @Test
    void posTransactionDtoCopiesTheVisibleFields() {
        PosTransaction t = new PosTransaction();
        t.setId(9L);
        t.setBusinessId(1L);
        t.setProvider(PaymentProvider.SNAPSCAN);
        t.setProviderTransactionId("snap_secret_ref");
        t.setAmount(new BigDecimal("149.90"));
        t.setPaymentMethod(PaymentMethod.APPLE_PAY);
        t.setStatus(PosTransactionStatus.SUCCESS);
        t.setCreatedAt(AT);

        PosTransactionDTO dto = PosTransactionDTO.from(t);
        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getProvider()).isEqualTo(PaymentProvider.SNAPSCAN);
        assertThat(dto.getAmount()).isEqualByComparingTo("149.90");
        assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.APPLE_PAY);
        assertThat(dto.getStatus()).isEqualTo(PosTransactionStatus.SUCCESS);
        assertThat(dto.getCreatedAt()).isEqualTo(AT);
        assertThat(dto.toString()).doesNotContain("snap_secret_ref");
    }

    @Test
    void productDtoCopiesEveryField() {
        Product p = new Product();
        p.setId(1L);
        p.setBusinessId(2L);
        p.setName("Milk");
        p.setBarcode("600111");
        p.setQrCode("QR-1");
        p.setQuantity(12);
        p.setMinStockLevel(4);
        p.setUnitPrice(new BigDecimal("19.99"));
        p.setSupplierId(3L);

        ProductDTO dto = ProductDTO.from(p);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBusinessId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Milk");
        assertThat(dto.getBarcode()).isEqualTo("600111");
        assertThat(dto.getQrCode()).isEqualTo("QR-1");
        assertThat(dto.getQuantity()).isEqualTo(12);
        assertThat(dto.getMinStockLevel()).isEqualTo(4);
        assertThat(dto.getUnitPrice()).isEqualByComparingTo("19.99");
        assertThat(dto.getSupplierId()).isEqualTo(3L);
    }

    @Test
    void supplierDtoCopiesEveryField() {
        Supplier s = new Supplier();
        s.setId(1L);
        s.setBusinessId(2L);
        s.setName("Acme");
        s.setPhone("0821234567");
        s.setEmail("sales@acme.test");
        SupplierDTO dto = SupplierDTO.from(s);
        assertThat(dto.getName()).isEqualTo("Acme");
        assertThat(dto.getPhone()).isEqualTo("0821234567");
        assertThat(dto.getEmail()).isEqualTo("sales@acme.test");
        assertThat(dto.getBusinessId()).isEqualTo(2L);
    }

    @Test
    void stockMovementDtoShowsWhatWhenAndWho() {
        StockMovement m = new StockMovement();
        m.setId(1L);
        m.setProductId(2L);
        m.setQuantity(24);
        m.setType(StockMovementType.IN);
        m.setScannedBy(7L);
        m.setScannedAt(AT);
        StockMovementDTO dto = StockMovementDTO.from(m);
        assertThat(dto.getProductId()).isEqualTo(2L);
        assertThat(dto.getQuantity()).isEqualTo(24);
        assertThat(dto.getType()).isEqualTo(StockMovementType.IN);
        assertThat(dto.getScannedBy()).isEqualTo(7L);
        assertThat(dto.getScannedAt()).isEqualTo(AT);
    }

    @Test
    void stockAlertAndAuditDtosCopyEveryField() {
        StockAlert a = new StockAlert();
        a.setId(1L);
        a.setProductId(2L);
        a.setCurrentQuantity(1);
        a.setMinStockLevel(5);
        a.setStatus(StockAlertStatus.RESOLVED);
        a.setCreatedAt(AT);
        StockAlertDTO ad = StockAlertDTO.from(a);
        assertThat(ad.getCurrentQuantity()).isEqualTo(1);
        assertThat(ad.getMinStockLevel()).isEqualTo(5);
        assertThat(ad.getStatus()).isEqualTo(StockAlertStatus.RESOLVED);

        AuditLog l = new AuditLog();
        l.setId(1L);
        l.setUserId(7L);
        l.setAction("UPDATE");
        l.setEntityType("Product");
        l.setEntityId(42L);
        l.setCreatedAt(AT);
        AuditLogDTO ld = AuditLogDTO.from(l);
        assertThat(ld.getAction()).isEqualTo("UPDATE");
        assertThat(ld.getEntityType()).isEqualTo("Product");
        assertThat(ld.getEntityId()).isEqualTo(42L);
    }

    @Test
    void totalsDtoCarriesTheRunningTotal() {
        PosTotalsDTO dto = PosTotalsDTO.of(new BigDecimal("350.50"), 2);
        assertThat(dto.getTotalPaid()).isEqualByComparingTo("350.50");
        assertThat(dto.getSuccessfulPayments()).isEqualTo(2);
    }
}
