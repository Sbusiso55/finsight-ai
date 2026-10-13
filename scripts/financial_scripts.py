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


def calculate_gross_profit(total_revenue, cost_of_goods_sold):
    """Calculate gross profit."""
    return total_revenue - cost_of_goods_sold


def calculate_gross_margin(total_revenue, cost_of_goods_sold):
    """Calculate gross margin percentage."""
    if total_revenue == 0:
        return 0

    gross_profit = calculate_gross_profit(
        total_revenue, cost_of_goods_sold
    )
    return (gross_profit / total_revenue) * 100


def calculate_net_profit(total_revenue, total_expenses):
    """Calculate net profit."""
    return total_revenue - total_expenses


def calculate_net_margin(total_revenue, total_expenses):
    """Calculate net margin percentage."""
    if total_revenue == 0:
        return 0

    net_profit = calculate_net_profit(total_revenue, total_expenses)
    return (net_profit / total_revenue) * 100


def calculate_net_cash_flow(cash_inflows, cash_outflows):
    """Calculate net cash flow."""
    return cash_inflows - cash_outflows


    return previous_closing_balance
def calculate_opening_balance(previous_closing_balance):
    """Use the previous period's closing balance as the opening balance."""
    return previous_closing_balance
def calculate_closing_balance(opening_balance, net_cash_flow):
    """Calculate closing cash balance."""
    return opening_balance + net_cash_flow


def forecast_next_month(historical_values):
    """Forecast next month's value using the average monthly change."""
    if not historical_values:
        return 0

    if len(historical_values) < 2:
        return historical_values[-1]

    monthly_changes = [
        historical_values[i] - historical_values[i - 1]
        for i in range(1, len(historical_values))
    ]

    average_change = sum(monthly_changes) / len(monthly_changes)
    return historical_values[-1] + average_change



def calculate_forecast_range(forecast_value, variation=0.10):
    """Return the lower and upper forecast range."""
    return {
        "lower": forecast_value * (1 - variation),
        "upper": forecast_value * (1 + variation)
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

    # Test profit margin calculations
    cost_of_goods_sold = 5000

    gross_profit = calculate_gross_profit(total_revenue, cost_of_goods_sold)
    gross_margin = calculate_gross_margin(total_revenue, cost_of_goods_sold)
    net_profit = calculate_net_profit(total_revenue, total_expenses)
    net_margin = calculate_net_margin(total_revenue, total_expenses)

    print("Gross profit:", gross_profit)
    print("Gross margin:", round(gross_margin, 2), "%")
    print("Net profit:", net_profit)
    print("Net margin:", round(net_margin, 2), "%")

    # Test cash flow calculations
    opening_balance = calculate_opening_balance(15000)
    net_cash_flow = calculate_net_cash_flow(total_revenue, total_expenses)
    closing_balance = calculate_closing_balance(opening_balance, net_cash_flow)

    print("Opening balance:", opening_balance)
    print("Net cash flow:", net_cash_flow)
    print("Closing balance:", closing_balance)


    # Test forecasting calculations
    historical_revenue = [22000, 25000, 30000]
    historical_expenses = [19000, 21000, 24800]

    forecast_revenue = forecast_next_month(historical_revenue)
    forecast_expenses = forecast_next_month(historical_expenses)
    forecast_profit = forecast_revenue - forecast_expenses

    revenue_range = calculate_forecast_range(forecast_revenue)
    expense_range = calculate_forecast_range(forecast_expenses)

    profit_range = {
        "lower": revenue_range["lower"] - expense_range["upper"],
        "upper": revenue_range["upper"] - expense_range["lower"]
    }

    print("Forecast revenue:", forecast_revenue)
    print("Forecast expenses:", forecast_expenses)
    print("Forecast profit:", forecast_profit)
    print("Revenue forecast range:", revenue_range)
    print("Expense forecast range:", expense_range)
    print("Profit forecast range:", profit_range)

