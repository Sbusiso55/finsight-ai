
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, field_validator

from scripts.financial_scripts import (
    forecast_next_month,
    calculate_forecast_range,
)

from scripts.openai_integration import (
    build_financial_summary,
    ask_financial_question,
)

app = FastAPI(title="FinSight AI API")


# Financial Analysis Request
class FinancialAnalysisRequest(BaseModel):
    transactions: list[dict]
    income: float = Field(ge=0)
    expenses: float = Field(ge=0)

    @field_validator("transactions")
    @classmethod
    def validate_transactions(cls, values):
        if not values:
            raise ValueError("transactions list cannot be empty")
        return values


# Forecast Request
class ForecastRequest(BaseModel):
    historical_values: list[float]

    @field_validator("historical_values")
    @classmethod
    def validate_months(cls, values):
        if len(values) < 3:
            raise ValueError("minimum 3 months of data required")
        return values


# Health Check
@app.get("/health")
def health_check():
    return {"status": "UP"}


# Existing Request Models
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


# Forecast Endpoint
@app.post("/api/forecast")
def forecast(request: ForecastRequest):
    try:
        forecast_value = forecast_next_month(
            request.historical_values
        )

        forecast_range = calculate_forecast_range(
            forecast_value
        )

        return {
            "forecast": forecast_value,
            "range": forecast_range
        }

    except Exception as error:
        raise HTTPException(
            status_code=500,
            detail=str(error)
        )


# Financial Analysis Endpoint
@app.post("/api/financial-analysis")
def financial_analysis(request: FinancialAnalysisRequest):
    profit = request.income - request.expenses

    return {
        "income": request.income,
        "expenses": request.expenses,
        "profit": profit,
        "transactions_count": len(request.transactions)
    }


# Existing Financial Summary Endpoint
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
        raise HTTPException(
            status_code=500,
            detail=str(error)
        )


# Existing AI Question Endpoint
@app.post("/ask-ai")
def ask_ai(request: AskAIRequest):
    try:
        answer = ask_financial_question(
            request.question,
            request.financial_data,
        )

        return {"answer": answer}

    except ValueError as error:
        raise HTTPException(
            status_code=400,
            detail=str(error)
        )

    except Exception as error:
        raise HTTPException(
            status_code=500,
            detail=str(error)
        )
