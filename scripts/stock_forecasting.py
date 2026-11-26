from datetime import datetime, timedelta
from math import ceil


def parse_movement_time(value):
    if isinstance(value, datetime):
        return value

    if isinstance(value, str):
        return datetime.fromisoformat(value.replace("Z", "+00:00"))

    raise ValueError("Invalid stock movement timestamp.")


def calculate_stock_forecast(
    product,
    stock_movements,
    supplier_lead_time_days=0,
    today=None,
):
    required_product_fields = [
        "id",
        "businessId",
        "name",
        "quantity",
        "minStockLevel",
    ]

    for field in required_product_fields:
        if field not in product:
            raise ValueError(f"Missing product field: {field}")

    if not isinstance(stock_movements, list):
        raise ValueError("Stock movements must be a list.")

    if supplier_lead_time_days < 0:
        raise ValueError("Supplier lead time cannot be negative.")

    today = today or datetime.now()
    if isinstance(today, datetime):
        today = today.date()

    product_id = product["id"]
    current_quantity = product["quantity"]
    minimum_stock = product["minStockLevel"]

    if current_quantity < 0 or minimum_stock < 0:
        raise ValueError("Stock quantities cannot be negative.")

    outgoing_movements = []

    for movement in stock_movements:
        required_fields = [
            "id",
            "productId",
            "quantity",
            "type",
            "scannedBy",
            "scannedAt",
        ]

        for field in required_fields:
            if field not in movement:
                raise ValueError(f"Missing stock movement field: {field}")

        if movement["type"] not in ("IN", "OUT"):
            raise ValueError("Movement type must be IN or OUT.")

        if movement["quantity"] <= 0:
            raise ValueError("Movement quantity must be greater than zero.")

        if movement["productId"] != product_id:
            continue

        if movement["type"] == "OUT":
            movement_date = parse_movement_time(
                movement["scannedAt"]
            ).date()

            outgoing_movements.append(
                {
                    "quantity": movement["quantity"],
                    "date": movement_date,
                }
            )

    reorder_recommended = current_quantity <= minimum_stock

    if not outgoing_movements:
        return {
            "productId": product_id,
            "businessId": product["businessId"],
            "productName": product["name"],
            "currentQuantity": current_quantity,
            "minStockLevel": minimum_stock,
            "supplierId": product.get("supplierId"),
            "averageDailyUsage": 0,
            "estimatedDaysUntilStockout": None,
            "predictedStockoutDate": None,
            "reorderRecommended": reorder_recommended,
            "reorderByDate": today.isoformat() if reorder_recommended else None,
            "supplierLeadTimeDays": supplier_lead_time_days,
            "message": "Insufficient outgoing stock history to predict demand.",
        }

    first_date = min(item["date"] for item in outgoing_movements)
    observation_days = max(1, (today - first_date).days + 1)

    total_used = sum(
        item["quantity"] for item in outgoing_movements
    )

    average_daily_usage = total_used / observation_days

    days_until_stockout = (
        current_quantity / average_daily_usage
    )

    stockout_date = today + timedelta(
        days=ceil(days_until_stockout)
    )

    days_until_reorder_level = (
        (current_quantity - minimum_stock) / average_daily_usage
    )

    days_before_reorder = max(
        0,
        ceil(days_until_reorder_level - supplier_lead_time_days),
    )

    reorder_date = today + timedelta(
        days=days_before_reorder
    )

    if days_until_reorder_level <= supplier_lead_time_days:
        reorder_recommended = True

    return {
        "productId": product_id,
        "businessId": product["businessId"],
        "productName": product["name"],
        "currentQuantity": current_quantity,
        "minStockLevel": minimum_stock,
        "supplierId": product.get("supplierId"),
        "averageDailyUsage": round(average_daily_usage, 2),
        "estimatedDaysUntilStockout": round(days_until_stockout, 2),
        "predictedStockoutDate": stockout_date.isoformat(),
        "reorderRecommended": reorder_recommended,

        "reorderByDate": reorder_date.isoformat(),
        "supplierLeadTimeDays": supplier_lead_time_days,
        "message": (
            "Reorder stock soon."
            if reorder_recommended
            else "Stock levels are currently sufficient."
        ),
    }
