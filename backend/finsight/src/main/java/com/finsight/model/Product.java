package com.finsight.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A stock item a business sells. Barcode and QR code are each unique within one business
 * (either may be empty, but never duplicated).
 */
@Entity
@Table(name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_business_barcode", columnNames = {"business_id", "barcode"}),
                @UniqueConstraint(name = "uk_products_business_qr_code", columnNames = {"business_id", "qr_code"})
        },
        indexes = @Index(name = "idx_products_supplier", columnList = "supplierId"))
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private String name;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer minStockLevel = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    private Long supplierId;
}
