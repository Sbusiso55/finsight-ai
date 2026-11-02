
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from scripts.openai_integration import (
    build_financial_summary,
    ask_financial_question,
)

app = FastAPI(title="FinSight AI API")


@app.get("/health")
def health_check():
    return {"status": "UP"}


class RevenueTransaction(BaseModel):
    amount: float = Field(ge=0)


class FinancialSummaryRequest(BaseModel):
    revenue_transactions: list[RevenueTransaction]
    expense_breakdown: dict[str, float]
    previous_revenue: float = Field(ge=0)
    total_fixed_expenses: float = Field(ge=0)
    total_variable_expenses: float = Field(ge=0)


class AskAIRequest(BaseModel):
    question: str
    financial_data: dict


@app.post("/financial-summary")
def financial_summary(request: FinancialSummaryRequest):
    try:
        revenue_transactions = [
            transaction.model_dump()
            for transaction in request.revenue_transactions
        ]

        return build_financial_summary(
            revenue_transactions=revenue_transactions,
            expense_breakdown=request.expense_breakdown,
            previous_revenue=request.previous_revenue,
            total_fixed_expenses=request.total_fixed_expenses,
            total_variable_expenses=request.total_variable_expenses,
        )

    except Exception as error:
        raise HTTPException(status_code=500, detail=str(error))


@app.post("/ask-ai")
def ask_ai(request: AskAIRequest):
    try:
        answer = ask_financial_question(
            request.question,
            request.financial_data,
        )
        return {"answer": answer}

    except ValueError as error:
        raise HTTPException(status_code=400, detail=str(error))

    except Exception as error:
        raise HTTPException(status_code=500, detail=str(error))
