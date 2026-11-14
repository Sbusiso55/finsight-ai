from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.middleware import SlowAPIMiddleware
from slowapi.util import get_remote_address
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
app = FastAPI(
    title="FinSight AI API",
    description="API for financial analysis, forecasting, and AI-powered financial insights.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)
# Rate Limiting Configuration
limiter = Limiter(key_func=get_remote_address)

app.state.limiter = limiter
app.add_exception_handler(
    RateLimitExceeded,
    _rate_limit_exceeded_handler,
)
app.add_middleware(SlowAPIMiddleware)
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
@app.post(
    "/api/forecast",
    summary="Forecast next month's revenue",
    description=(
        "Uses at least three months of historical financial values "
        "to predict next month's value and calculate an estimated range. "
        "Limited to 10 requests per minute per IP address."
    ),
    response_description="Predicted value and estimated forecast range.",
    responses={
        200: {"description": "Forecast generated successfully."},
        422: {"description": "Invalid input or insufficient historical data."},
        429: {"description": "Rate limit exceeded. Maximum 10 requests per minute."},
        500: {"description": "An internal error occurred while generating the forecast."},
    },
)
@limiter.limit("10/minute")
def forecast(request: Request, data: ForecastRequest):
    try:
        forecast_value = forecast_next_month(
            data.historical_values
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
@app.post(
    "/api/financial-analysis",
    summary="Analyse business financial performance",
    description=(
        "Calculates profit by subtracting expenses from income "
        "and returns the number of transactions provided. "
        "Limited to 20 requests per minute per IP address."
    ),
    response_description="Income, expenses, calculated profit and transaction count.",
    responses={
        200: {"description": "Financial analysis completed successfully."},
        422: {"description": "Invalid financial data or empty transactions list."},
        429: {"description": "Rate limit exceeded. Maximum 20 requests per minute."},
    },
)
@limiter.limit("20/minute")
def financial_analysis(request: Request, data: FinancialAnalysisRequest):
    profit = data.income - data.expenses

    return {
        "income": data.income,
        "expenses": data.expenses,
        "profit": profit,
        "transactions_count": len(data.transactions),
    }


# Existing Financial Summary Endpoint
@app.post(
    "/financial-summary",
    summary="Generate a financial summary",
    description=(
        "Generates an AI-powered financial summary using revenue "
        "transactions, expense breakdowns, previous revenue, "
        "and fixed and variable expenses. "
        "Limited to 10 requests per minute per IP address."
    ),
    response_description="Financial summary and insights generated from the supplied data.",
    responses={
        200: {"description": "Financial summary generated successfully."},
        422: {"description": "Invalid financial summary input."},
        429: {"description": "Rate limit exceeded. Maximum 10 requests per minute."},
        500: {"description": "An internal error occurred while generating the summary."},
    },
)
@limiter.limit("10/minute")
def financial_summary(request: Request, data: FinancialSummaryRequest):
    try:
        revenue_transactions = [
            transaction.model_dump()
            for transaction in data.revenue_transactions
        ]

        return build_financial_summary(
            revenue_transactions=revenue_transactions,
            expense_breakdown=data.expense_breakdown,
            previous_revenue=data.previous_revenue,
            total_fixed_expenses=data.total_fixed_expenses,
            total_variable_expenses=data.total_variable_expenses,
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
@app.post(
    "/ask-ai",
    summary="Ask AI a financial question",
    description=(
        "Accepts a financial question and relevant financial data, "
        "then returns an AI-generated answer. "
        "Limited to 5 requests per minute per IP address."
    ),
    response_description="AI-generated answer to the financial question.",
    responses={
        200: {"description": "AI answer generated successfully."},
        400: {"description": "The financial question could not be processed."},
        422: {"description": "Invalid request data."},
        429: {"description": "Rate limit exceeded. Maximum 5 requests per minute."},
        500: {"description": "An internal error occurred while generating the answer."},
    },
)
@limiter.limit("5/minute")
def ask_ai(request: Request, data: AskAIRequest):
    try:
        answer = ask_financial_question(
            data.question,
            data.financial_data,
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
