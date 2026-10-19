
import os
from unittest.mock import patch

from dotenv import load_dotenv
from google import genai
from google.genai import types

from financial_scripts import (
    calculate_total_revenue,
    calculate_revenue_growth_rate,
    calculate_total_expenses,
    calculate_expense_breakdown,
    calculate_expense_to_revenue_ratio,
    calculate_fixed_variable_split,
)

load_dotenv()


def build_financial_summary(
    revenue_transactions,
    expense_breakdown,
    previous_revenue,
    total_fixed_expenses,
    total_variable_expenses,
):
    """Build a financial summary using existing calculation functions."""

    total_revenue = calculate_total_revenue(revenue_transactions)
    total_expenses = calculate_total_expenses(expense_breakdown)

    return {
        "total_revenue": total_revenue,
        "previous_revenue": previous_revenue,
        "revenue_growth_percent": calculate_revenue_growth_rate(
            total_revenue, previous_revenue
        ),
        "total_expenses": total_expenses,
        "expense_breakdown": calculate_expense_breakdown(
            expense_breakdown
        ),
        "expense_to_revenue_percent": calculate_expense_to_revenue_ratio(
            total_expenses, total_revenue
        ),
        "fixed_variable_split_percent": calculate_fixed_variable_split(
            total_fixed_expenses, total_variable_expenses
        ),
    }


def ask_financial_question(question, financial_data):
    """Answer a financial question using Gemini."""

    api_key = os.getenv("GEMINI_API_KEY")

    if not api_key:
        raise ValueError(
            "GEMINI_API_KEY is not set. Configure your API key first."
        )

    if not isinstance(question, str) or not question.strip():
        raise ValueError("Please enter a financial question.")

    if not isinstance(financial_data, dict):
        raise ValueError("Financial data must be provided as a dictionary.")

    client = genai.Client(api_key=api_key)

    response = client.models.generate_content(
        model="gemini-3.5-flash-lite",
        config=types.GenerateContentConfig(
            system_instruction=(
                "You are FinSight, a financial intelligence assistant. "
                "Answer questions conversationally and clearly. "
                "Use the supplied financial data for specific figures. "
                "Do not invent missing figures or present estimates as facts. "
                "Explain relevant trends, risks, and practical suggestions."
            )
        ),
        contents=(
            f"Financial data:\n{financial_data}\n\n"
            f"User's question:\n{question}"
        ),
    )

    if not response.text:
        raise RuntimeError("Gemini returned an empty response.")

    return response.text


if __name__ == "__main__":
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

    financial_data = build_financial_summary(
        revenue_transactions=revenue_transactions,
        expense_breakdown=expense_breakdown,
        previous_revenue=25000,
        total_fixed_expenses=15000,
        total_variable_expenses=9800,
    )

    with patch("__main__.genai.Client") as mock_client_factory:
        mock_client = mock_client_factory.return_value
        mock_response = mock_client.models.generate_content.return_value
        mock_response.text = (
            "Your business generated R30,000 in revenue and "
            "R24,800 in expenses. Revenue grew by 20%."
        )

        answer = ask_financial_question(
            "How is my business doing?",
            financial_data,
        )

        print("Mock test response:")
        print(answer)
        print("Financial data sent to the assistant:")

        print(financial_data)
