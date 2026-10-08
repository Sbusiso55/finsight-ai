package com.finsight.model;

// Entity for the businesses table

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A business whose finances are tracked. One user can own several. */
@Entity
@Table(name = "businesses")
@Getter
@Setter
public class Business extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String industry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 3)
    private String currency = "ZAR";

    /** Cash on hand at openingBalanceDate (e.g. R15 000 on 2026-03-01). Starting point for cash flow. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private LocalDate openingBalanceDate;

    /** Seasonal businesses need a full year of data before forecasts are trustworthy. */
    @Column(nullable = false)
    private boolean seasonal = false;
}
