package com.finsight.model;

// Entity for the users table


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/** A person who can log in to FinSight. */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    /** Used for the WhatsApp integration. */
    private String phoneNumber;

    @Column(nullable = false)
    private boolean enabled = true;
}