
import logging
import time

from fastapi import FastAPI, HTTPException, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException
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

# Server statistics
START_TIME = time.time()
total_requests = 0

# Error logging configuration
logging.basicConfig(
    filename="api_errors.log",
    level=logging.ERROR,
    format="%(asctime)s | %(levelname)s | %(message)s",
    encoding="utf-8",
)

logger = logging.getLogger(__name__)


# Request Counter Middleware
@app.middleware("http")
async def count_requests(request: Request, call_next):
    global total_requests

    total_requests += 1
    response = await call_next(request)

    return response


# HTTP Exception Handler
@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    logger.error(
        "Endpoint: %s | Status: %s | Error: %s",
        request.url.path,
        exc.status_code,
        exc.detail,
    )

    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail},
        headers=exc.headers,
    )


# Request Validation Error Handler
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.error(
        "Endpoint: %s | Validation Error: %s",
        request.url.path,
        exc.errors(),
    )

    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors()},
    )


# Request Validation Error Handler
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.error(
        "Endpoint: %s | Validation Error: %s",
        request.url.path,
        exc.errors(),
    )

    return JSONResponse(
        status_code=422,
        content=jsonable_encoder(
            {"detail": exc.errors()},
            custom_encoder={ValueError: str},
        ),
    )

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


# API Status Endpoint
@app.get("/api/status")
def api_status():
    uptime = time.time() - START_TIME

    return {
        "status": "UP",
        "uptime_seconds": round(uptime, 2),
        "total_requests": total_requests,
    }


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
            "range": forecast_range,
        }

    except Exception as error:
        logger.exception(
            "Error in /api/forecast: %s",
            str(error),
        )
        raise HTTPException(
            status_code=500,
            detail=str(error),
        )


# Financial Analysis Endpoint
@app.post("/api/financial-analysis")
def financial_analysis(request: FinancialAnalysisRequest):
    profit = request.income - request.expenses

    return {
        "income": request.income,
        "expenses": request.expenses,
        "profit": profit,
        "transactions_count": len(request.transactions),
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
        logger.exception(
            "Error in /financial-summary: %s",
            str(error),
        )
        raise HTTPException(
            status_code=500,
            detail=str(error),
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
        logger.exception(
            "Error in /ask-ai: %s",
            str(error),
        )
        raise HTTPException(
            status_code=400,
            detail=str(error),
        )

    except Exception as error:
        logger.exception(
            "Error in /ask-ai: %s",
            str(error),
        )
        raise HTTPException(
            status_code=500,
            detail=str(error),
        )
