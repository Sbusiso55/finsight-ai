import os
from dotenv import load_dotenv
from openai import OpenAI
from unittest.mock import patch
load_dotenv()



def ask_financial_question(question, financial_data):
    """Answer a question using the user's financial data."""

    api_key = os.getenv("OPENAI_API_KEY")

    if not api_key:
        raise ValueError(
            "OPENAI_API_KEY is not set. Configure your API key first."
        )

    if not isinstance(question, str) or not question.strip():
        raise ValueError("Please enter a financial question.")

    if not isinstance(financial_data, dict):
        raise ValueError("Financial data must be provided as a dictionary.")

    client = OpenAI(api_key=api_key)

    response = client.responses.create(
        model="gpt-4.1-mini",
        instructions=(
            "You are FinSight, a financial intelligence assistant. "
            "Explain financial information clearly and conversationally. "
            "Use only the supplied financial data for specific figures. "
            "Do not invent missing figures. Explain trends and risks, "
            "and provide practical general financial suggestions. "
            "Do not present estimates as guaranteed outcomes."
        ),
        input=(
            f"Financial data:\n{financial_data}\n\n"
            f"User's question:\n{question}"
        ),
    )

    return response.output_text



if __name__ == "__main__":
    sample_financial_data = {
        "revenue": 30000,
        "expenses": 24800,
        "net_profit": 5200,
        "gross_margin_percent": 83.33,
        "net_margin_percent": 17.33,
        "net_cash_flow": 5200,
        "closing_balance": 20200,
    }

    with patch("__main__.OpenAI") as mock_openai:
        mock_client = mock_openai.return_value
        mock_response = mock_client.responses.create.return_value
        mock_response.output_text = (
            "Your business has R30,000 revenue, "
            "R24,800 expenses, and R5,200 net profit."
        )

        answer = ask_financial_question(
            "How is my business doing?",
            sample_financial_data,
        )
        print("Mock test response:")
        print(answer)


