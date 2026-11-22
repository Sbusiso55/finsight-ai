package com.finsight.model;

import com.finsight.model.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** One scan: what stock came in or went out, how many, when, and who scanned it. */
@Entity
@Table(name = "stock_movements",
        indexes = @Index(name = "idx_stock_movements_product_time", columnList = "product_id, scannedAt"))
@Getter
@Setter
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Column(nullable = false)
    private Long scannedBy;

    @Column(nullable = false)
    private LocalDateTime scannedAt;

    @PrePersist
    void onCreate() {
        if (scannedAt == null) scannedAt = LocalDateTime.now();
    }
}
