# ============================================================
# FINSIGHT - FINANCIAL SCRIPT FUNCTIONS
# ============================================================

# Sample data for testing

revenue_transactions = [
    {"amount": 8000},
    {"amount": 12500},
    {"amount": 4500},
    {"amount": 5000},
]

expense_breakdown = {
    "Salaries": 9000,
    "Rent": 6000,
    "Advertising": 2500,
    "Utilities": 2300,
    "Other business costs": 5000,
}

february_revenue = 25000


def calculate_total_revenue(transactions):
    """Calculate total revenue."""
    return sum(transaction["amount"] for transaction in transactions)


def calculate_revenue_growth_rate(current_revenue, previous_revenue):
    """Calculate revenue growth rate percentage."""
    if previous_revenue == 0:
        return 0
    return ((current_revenue - previous_revenue) / previous_revenue) * 100


def calculate_total_expenses(expense_breakdown):
    """Calculate total expenses."""
    return sum(expense_breakdown.values())


def calculate_expense_breakdown(expense_breakdown):
    """Return expenses by category."""
    return expense_breakdown


def calculate_expense_to_revenue_ratio(total_expenses, total_revenue):
    """Calculate expense-to-revenue ratio percentage."""
    if total_revenue == 0:
        return 0
    return (total_expenses / total_revenue) * 100


def calculate_fixed_variable_split(expense_breakdown):
    """Calculate fixed and variable expenses."""

    fixed_expenses = (
        expense_breakdown.get("Salaries", 0)
        + expense_breakdown.get("Rent", 0)
    )

    variable_expenses = (
        calculate_total_expenses(expense_breakdown)
        - fixed_expenses
    )

    return {
        "fixed_expenses": fixed_expenses,
        "variable_expenses": variable_expenses
    }


# ============================================================
# EXAMPLE USAGE
# ============================================================

total_revenue = calculate_total_revenue(revenue_transactions)

revenue_growth_rate = calculate_revenue_growth_rate(
    total_revenue,
    february_revenue
)

total_expenses = calculate_total_expenses(expense_breakdown)

expense_categories = calculate_expense_breakdown(
    expense_breakdown
)

expense_ratio = calculate_expense_to_revenue_ratio(
    total_expenses,
    total_revenue
)

fixed_variable_split = calculate_fixed_variable_split(
    expense_breakdown
)

print("Total Revenue:", total_revenue)
print("Revenue Growth Rate:", round(revenue_growth_rate, 2), "%")
print("Total Expenses:", total_expenses)
print("Expense Breakdown:", expense_categories)
print("Expense to Revenue Ratio:", round(expense_ratio, 2), "%")
print("Fixed vs Variable Split:", fixed_variable_split)