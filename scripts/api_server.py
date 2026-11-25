from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.middleware import SlowAPIMiddleware
from slowapi.util import get_remote_address
from typing import Optional
import logging
import time

from fastapi import (
    FastAPI,
    HTTPException,
    Request,
    UploadFile,
    File,
    Form,
)
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
from scripts.fraud_detection import detect_payment_fraud
from scripts.stock_forecasting import calculate_stock_forecast
from scripts.pos_import import import_pos_csv
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


class PosTransactionRequest(BaseModel):
    id: int
    businessId: int
    provider: str
    providerTransactionId: str
    amount: float = Field(gt=0)
    paymentMethod: str
    status: str
    createdAt: str


class FraudDetectionRequest(BaseModel):
    transactions: list[PosTransactionRequest]

    @field_validator("transactions")
    @classmethod
    def validate_transactions(cls, values):
        if not values:
            raise ValueError("At least one transaction is required.")
        return values


class ProductRequest(BaseModel):
    id: int
    businessId: int
    name: str
    barcode: Optional[str] = None
    qrCode: Optional[str] = None
    quantity: int = Field(ge=0)
    minStockLevel: int = Field(ge=0)
    unitPrice: float = Field(ge=0)
    supplierId: Optional[int] = None


class StockMovementRequest(BaseModel):
    id: int
    productId: int
    quantity: int = Field(gt=0)
    type: str
    scannedBy: int
    scannedAt: str

    @field_validator("type")
    @classmethod
    def validate_movement_type(cls, value):
        if value not in ("IN", "OUT"):
            raise ValueError("Movement type must be IN or OUT.")
        return value


class StockForecastRequest(BaseModel):
    product: ProductRequest
    stockMovements: list[StockMovementRequest]
    supplierLeadTimeDays: int = Field(default=0, ge=0)



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

# Fraud Detection Endpoint
@app.post(
    "/api/fraud-detection",
    summary="Detect suspicious payment activity",
    description=(
        "Analyses POS transactions for unusually large payments, "
        "rapid payment bursts and unusual payment hours."
    ),
    response_description="Suspicious transactions and risk indicators.",
    responses={
        200: {"description": "Fraud analysis completed successfully."},
        400: {"description": "Invalid transaction data."},
        422: {"description": "Invalid request fields."},
        429: {"description": "Rate limit exceeded."},
        500: {"description": "An internal error occurred."},
    }
)
def fraud_detection(request: Request, data: FraudDetectionRequest):
    try:
        transactions = [
            transaction.model_dump()
            for transaction in data.transactions
        ]

        result = detect_payment_fraud(transactions)

        logger.info(
            "Fraud detection completed: transactions=%s suspicious=%s",
            result["totalTransactions"],
            result["suspiciousCount"],
        )

        return result

    except ValueError as error:
        logger.exception("Fraud detection validation error: %s", str(error))
        raise HTTPException(status_code=400, detail=str(error))

    except Exception as error:
        logger.exception("Error in /api/fraud-detection: %s", str(error))
        raise HTTPException(
            status_code=500,
            detail="An error occurred during fraud detection.",
        )

# Stock Forecasting Endpoint
@app.post(
    "/api/stock-forecast",
    summary="Forecast stock levels and reorder dates",
    description=(
        "Predicts when a product may run out of stock and "
        "recommends when the owner should reorder."
    ),
    response_description="Stock forecast and reorder recommendation.",
    responses={
        200: {"description": "Stock forecast generated successfully."},
        400: {"description": "Invalid stock data."},
        422: {"description": "Invalid request fields."},
        429: {"description": "Rate limit exceeded."},
        500: {"description": "An internal error occurred."},
    },
)
def stock_forecast(request: Request, data: StockForecastRequest):
    try:
        product = data.product.model_dump()

        stock_movements = [
            movement.model_dump()
            for movement in data.stockMovements
        ]

        result = calculate_stock_forecast(
            product,
            stock_movements,
            data.supplierLeadTimeDays,
        )

        logger.info(
            "Stock forecast completed: productId=%s reorderRecommended=%s",
            result["productId"],
            result["reorderRecommended"],
        )

        return result

    except ValueError as error:
        logger.exception("Stock forecasting validation error: %s", str(error))
        raise HTTPException(status_code=400, detail=str(error))

    except Exception as error:
        logger.exception("Error in /api/stock-forecast: %s", str(error))
        raise HTTPException(
            status_code=500,
            detail="An error occurred during stock forecasting.",
        )

# POS CSV Import Endpoint
@app.post(
    "/api/pos/import-csv",
    summary="Import POS transactions from CSV",
    description=(
        "Reads and validates transaction exports from "
        "Yoco, SnapScan and Zapper."
    ),
    responses={
        200: {"description": "CSV processed successfully."},
        400: {"description": "Invalid CSV file or data."},
        422: {"description": "Invalid request fields."},
        429: {"description": "Rate limit exceeded."},
        500: {"description": "An internal error occurred."},
    },
)
async def import_pos_transactions(
    request: Request,
    provider: str = Form(...),
    businessId: int = Form(...),
    file: UploadFile = File(...),
):
    try:
        if not file.filename or not file.filename.lower().endswith(".csv"):
            raise HTTPException(
                status_code=400,
                detail="Please upload a CSV file.",
            )

        content = await file.read(5 * 1024 * 1024 + 1)

        if len(content) > 5 * 1024 * 1024:
            raise HTTPException(
                status_code=400,
                detail="CSV file cannot exceed 5 MB.",
            )

        result = import_pos_csv(content, provider, businessId)

        logger.info(
            "POS CSV import completed: provider=%s businessId=%s imported=%s invalid=%s",
            provider,
            businessId,
            result["importedCount"],
            result["invalidCount"],
        )

        return result

    except HTTPException:
        raise

    except UnicodeDecodeError:
        logger.exception("POS CSV encoding error.")
        raise HTTPException(
            status_code=400,
            detail="CSV file must use UTF-8 encoding.",
        )

    except ValueError as error:
        logger.exception("POS CSV import validation error: %s", str(error))
        raise HTTPException(status_code=400, detail=str(error))

    except Exception as error:
        logger.exception("Error in POS CSV import: %s", str(error))
        raise HTTPException(
            status_code=500,
            detail="An error occurred during CSV import.",
        )

    finally:
        await file.close()
