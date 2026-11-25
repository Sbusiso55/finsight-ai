import pytest

from decimal import Decimal
from scripts.pos_import import import_pos_csv, parse_amount


@pytest.mark.parametrize(
    "value, expected",
    [
        ("149,90", "149.90"),
        ("149.90", "149.90"),
        ("R149,90", "149.90"),
        ("1 234,56", "1234.56"),
        ("1,234.56", "1234.56"),
        ("R 1 234,56", "1234.56"),
    ],
)
def test_parse_amount_formats(value, expected):
   assert parse_amount(value) == Decimal(expected)

def test_duplicate_transaction_ids_are_rejected():
    csv_content = (
        "transaction_id,amount,status,payment_method,created_at\n"
        "TX001,149.90,SUCCESS,CARD,2026-11-20 10:00:00\n"
        "TX001,200.00,SUCCESS,CARD,2026-11-20 11:00:00\n"
    )

    result = import_pos_csv(csv_content, "YOCO", 10)

    assert result["importedCount"] == 1
    assert result["invalidCount"] == 1
    assert result["rowErrors"][0]["error"] == "Duplicate transaction ID"


def test_snapscan_defaults_missing_payment_method_to_qr():
    csv_content = (
        "transaction_id,amount,status,created_at\n"
        "TX001,149.90,SUCCESS,2026-11-20 10:00:00\n"
    )

    result = import_pos_csv(csv_content, "SNAPSCAN", 10)

    assert result["importedCount"] == 1
    assert result["transactions"][0]["paymentMethod"] == "QR"


def test_bad_row_is_reported_without_stopping_import():
    csv_content = (
        "transaction_id,amount,status,payment_method,created_at\n"
        "TX001,149.90,SUCCESS,CARD,2026-11-20 10:00:00\n"
        "TX002,INVALID,SUCCESS,CARD,2026-11-20 11:00:00\n"
        "TX003,200.00,SUCCESS,CARD,2026-11-20 12:00:00\n"
    )

    result = import_pos_csv(csv_content, "YOCO", 10)

    assert result["totalRows"] == 3
    assert result["importedCount"] == 2
    assert result["invalidCount"] == 1
    assert result["rowErrors"][0]["row"] == 3
    assert result["transactions"][1]["providerTransactionId"] == "TX003"