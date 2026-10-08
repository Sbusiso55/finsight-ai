package com.finsight.model;

// Entity for transaction categories
import com.finsight.model.BaseEntity;
import com.finsight.model.enums.CostBehavior;
import com.finsight.model.enums.CostClassification;
import com.finsight.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Transaction category, e.g. "Sales revenue", "Consulting revenue", "Staff wages", "Shop rental".
 * costBehavior and costClassification only apply to EXPENSE categories; they drive the
 * fixed-vs-variable split and the gross/operating profit calculations.
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "name"}))
@Getter
@Setter
public class Category extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /** FIXED (rent, salaries) or VARIABLE (advertising, utilities). Null for income categories. */
    @Enumerated(EnumType.STRING)
    private CostBehavior costBehavior;

    /** COGS or operating expense. Null for income categories. */
    @Enumerated(EnumType.STRING)
    private CostClassification costClassification;
}