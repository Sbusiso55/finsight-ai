package com.finsight.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** A paying customer, e.g. "Anita's Retail". Revenue transactions can point to one. */
@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phoneNumber;
}
