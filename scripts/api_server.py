
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from scripts.openai_integration import (
    build_financial_summary,
    ask_financial_question,
)

app = FastAPI(
    title="FinSight Financial API",
    description="API for financial summaries and AI-powered financial questions.",
    version="1.0.0",
)


class FinancialQuestion(BaseModel):
    question: str = Field(..., min_length=1)
    financial_data: dict


@app.get("/")
def health_check():
    return {"status": "FinSight API is running"}


@app.get("/financial-summary")
def get_financial_summary():
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

    try:
        return build_financial_summary(
            revenue_transactions=revenue_transactions,
            expense_breakdown=expense_breakdown,
            previous_revenue=25000,
            total_fixed_expenses=15000,
            total_variable_expenses=9800,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Unable to generate financial summary.",
        ) from exc


@app.post("/ask-ai")
def ask_ai(request: FinancialQuestion):
    try:
        answer = ask_financial_question(
            request.question,
            request.financial_data,
        )
        return {"answer": answer}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail="The AI service could not answer the question.",
        ) from exc
