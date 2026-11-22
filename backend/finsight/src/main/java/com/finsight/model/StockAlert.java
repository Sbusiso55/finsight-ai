package com.finsight.model;

import com.finsight.model.enums.StockAlertStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** A low stock warning for one product. */
@Entity
@Table(name = "stock_alerts",
        indexes = @Index(name = "idx_stock_alerts_product_status", columnList = "product_id, status"))
@Getter
@Setter
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(nullable = false)
    private Integer minStockLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StockAlertStatus status = StockAlertStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
