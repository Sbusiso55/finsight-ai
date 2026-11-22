package com.finsight.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A company that supplies stock to a business. */
@Entity
@Table(name = "suppliers",
        indexes = @Index(name = "idx_suppliers_business", columnList = "business_id"))
@Getter
@Setter
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;
}
