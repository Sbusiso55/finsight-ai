package com.finsight.model;

import com.finsight.model.enums.InsightType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Threshold for one advice rule, per business. Defaults from the FinSight rules table:
 * LARGEST_EXPENSE 40, HIGH_FIXED_COSTS 60, HIGH_EXPENSE_RATIO 80, CATEGORY_SPIKE 20.
 * The two growth-comparison rules compare expense growth to revenue growth and need no threshold.
 */
@Entity
@Table(name = "alert_rules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "ruleType"}))
@Getter
@Setter
public class AlertRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightType ruleType;

    /** Percentage that triggers the rule. Null for rules that do not use one. */
    @Column(precision = 5, scale = 2)
    private BigDecimal thresholdPercent;

    @Column(nullable = false)
    private boolean enabled = true;
}
