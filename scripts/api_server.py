
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
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

# Allow the React frontend to call the API during local development.
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://localhost:5173",
    ],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


class RevenueTransaction(BaseModel):
    amount: float = Field(..., ge=0)


class FinancialSummaryRequest(BaseModel):
    revenue_transactions: list[RevenueTransaction]
    expense_breakdown: dict[str, float]
    previous_revenue: float = Field(..., ge=0)
    total_fixed_expenses: float = Field(..., ge=0)
    total_variable_expenses: float = Field(..., ge=0)


class FinancialQuestion(BaseModel):
    question: str = Field(..., min_length=1)
    financial_data: dict


@app.get("/")
def health_check():
    return {"status": "FinSight API is running"}


@app.post("/financial-summary")
def get_financial_summary(request: FinancialSummaryRequest):
    try:
        return build_financial_summary(
            revenue_transactions=[
                transaction.model_dump()
                for transaction in request.revenue_transactions
            ],
            expense_breakdown=request.expense_breakdown,
            previous_revenue=request.previous_revenue,
            total_fixed_expenses=request.total_fixed_expenses,
            total_variable_expenses=request.total_variable_expenses,
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
