package com.finsight.model;

import com.finsight.model.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Links a user to a business with a role. */
@Entity
@Table(name = "business_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "user_id"}))
@Getter
@Setter
public class BusinessMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
