
from datetime import datetime

from scripts.stock_forecasting import calculate_stock_forecast


TODAY = datetime(2026, 11, 25)


def make_product(
    quantity=40,
    min_stock_level=10,
    supplier_id=5,
):
    product = {
        "id": 1,
        "businessId": 10,
        "name": "Test Product",
        "quantity": quantity,
        "minStockLevel": min_stock_level,
    }

    if supplier_id is not None:
        product["supplierId"] = supplier_id

    return product


def make_movement(movement_id, quantity, date):
    return {
        "id": movement_id,
        "productId": 1,
        "quantity": quantity,
        "type": "OUT",
        "scannedBy": 100,
        "scannedAt": date,
    }


def test_no_outgoing_history_returns_no_stockout_prediction():
    result = calculate_stock_forecast(
        make_product(),
        [],
        today=TODAY,
    )

    assert result["averageDailyUsage"] == 0
    assert result["estimatedDaysUntilStockout"] is None
    assert result["predictedStockoutDate"] is None
    assert result["reorderRecommended"] is False


def test_normal_usage_predicts_correct_stockout_date():
    movements = [
        make_movement(1, 5, "2026-11-21T10:00:00"),
        make_movement(2, 5, "2026-11-22T10:00:00"),
        make_movement(3, 5, "2026-11-23T10:00:00"),
        make_movement(4, 5, "2026-11-24T10:00:00"),
    ]

    result = calculate_stock_forecast(
        make_product(quantity=40, min_stock_level=10),
        movements,
        supplier_lead_time_days=2,
        today=TODAY,
    )

    assert result["averageDailyUsage"] == 4
    assert result["estimatedDaysUntilStockout"] == 10
    assert result["predictedStockoutDate"] == "2026-12-05"
    assert result["reorderRecommended"] is False


def test_already_below_minimum_stock_recommends_reorder():
    movements = [
        make_movement(1, 5, "2026-11-25T10:00:00"),
    ]

    result = calculate_stock_forecast(
        make_product(quantity=5, min_stock_level=10),
        movements,
        today=TODAY,
    )

    assert result["reorderRecommended"] is True
    assert result["reorderByDate"] == "2026-11-25"


def test_product_without_supplier_is_supported():
    result = calculate_stock_forecast(
        make_product(supplier_id=None),
        [],
        today=TODAY,
    )

    assert result["supplierId"] is None
    assert result["productId"] == 1
