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


def calculate_fixed_variable_split(total_fixed_expenses, total_variable_expenses):
    """Calculate fixed and variable expense percentages."""
    total_expenses = total_fixed_expenses + total_variable_expenses

    if total_expenses == 0:
        return {
            "fixed_expenses": 0,
            "variable_expenses": 0
        }

    return {
        "fixed_expenses": (total_fixed_expenses / total_expenses) * 100,
        "variable_expenses": (total_variable_expenses / total_expenses) * 100
    }


if __name__ == "__main__":
    # Test data
    revenue_transactions = [
        {"amount": 8000},
        {"amount": 12500},
        {"amount": 4500},
        {"amount": 5000}
    ]

    expense_breakdown = {
        "Salaries": 9000,
        "Rent": 6000,
        "Advertising": 2500,
        "Utilities": 2300,
        "Other business costs": 5000
    }

    february_revenue = 25000

    # Test the calculation functions
    total_revenue = calculate_total_revenue(revenue_transactions)
    print("Total revenue:", total_revenue)

    revenue_growth = calculate_revenue_growth_rate(total_revenue, february_revenue)
    print("Revenue growth rate:", revenue_growth, "%")

    total_expenses = calculate_total_expenses(expense_breakdown)
    print("Total expenses:", total_expenses)

    print("Expense breakdown:", calculate_expense_breakdown(expense_breakdown))

    expense_ratio = calculate_expense_to_revenue_ratio(total_expenses, total_revenue)
    print("Expense-to-revenue ratio:", expense_ratio, "%")

    fixed_variable_split = calculate_fixed_variable_split(15000, 9800)
    print("Fixed/variable split:", fixed_variable_split)