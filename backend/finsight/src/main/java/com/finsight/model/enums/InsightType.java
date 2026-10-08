package com.finsight.model.enums;

/** One value per advice rule in the FinSight expense-insight table. */
public enum InsightType {
    LARGEST_EXPENSE,             // one category > 40% of total expenses
    HIGH_FIXED_COSTS,            // fixed costs > 60% of expenses
    HIGH_EXPENSE_RATIO,          // expenses > 80% of revenue
    CATEGORY_SPIKE,              // any category up > 20% month-on-month
    EXPENSES_OUTPACING_REVENUE,  // expense growth > revenue growth
    EFFICIENT_SCALING            // expense growth < revenue growth
}
