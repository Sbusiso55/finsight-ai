package com.finsight.model;

import com.finsight.model.enums.InsightSeverity;
import com.finsight.model.enums.InsightType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A piece of advice generated for a business and month, e.g.
 * "Your biggest expense is salaries at R9,000, making up 36.29% of your total spending."
 */
@Entity
@Table(name = "expense_insights",
        indexes = @Index(name = "idx_insights_business_period", columnList = "business_id, periodStart"))
@Getter
@Setter
public class ExpenseInsight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id")
    private Business business;

    /** First day of the month the insight is about. */
    @Column(nullable = false)
    private LocalDate periodStart;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightSeverity severity;

    /** Only set for category-specific insights (largest expense, category spike). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** The figure behind the advice, e.g. 36.29 (percent) or 1500.00 (rand). */
    @Column(precision = 15, scale = 2)
    private BigDecimal metricValue;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean dismissed = false;
}
