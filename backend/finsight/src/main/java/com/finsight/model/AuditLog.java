package com.finsight.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Who did what to which record, e.g. (user 7, "UPDATE", "Product", 42). Rows are only ever added. */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_user", columnList = "user_id"),
                @Index(name = "idx_audit_logs_entity", columnList = "entityType, entityId")
        })
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    private Long entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
