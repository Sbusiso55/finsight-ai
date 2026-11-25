import re
import unicodedata
import csv
import io
from datetime import datetime
from zoneinfo import ZoneInfo
JOHANNESBURG_TZ = ZoneInfo("Africa/Johannesburg")
from decimal import Decimal, InvalidOperation


SUPPORTED_PROVIDERS = {"YOCO", "SNAPSCAN", "ZAPPER"}

HEADER_ALIASES = {
    "providerTransactionId": [
        "transactionid",
        "transaction_id",
        "paymentid",
        "payment_id",
        "reference",
        "id",
    ],
    "amount": [
        "amount",
        "total",
        "transactionamount",
        "transaction_amount",
    ],
    "status": [
        "status",
        "paymentstatus",
        "payment_status",
    ],
    "paymentMethod": [
        "paymentmethod",
        "payment_method",
        "method",
        "channel",
    ],
    "createdAt": [
        "createdat",
        "created_at",
        "date",
        "timestamp",
        "transactiondate",
        "transaction_date",
    ],
}

VALID_STATUSES = {
    "PENDING",
    "SUCCESS",
    "FAILED",
    "REFUNDED",
}

VALID_PAYMENT_METHODS = {
    "TAP",
    "CHIP",
    "SWIPE",
    "APPLE_PAY",
    "GOOGLE_PAY",
    "SAMSUNG_PAY",
    "QR",
}


def normalize_header(value):
    return "".join(
        character.lower()
        for character in value
        if character.isalnum()
    )
def parse_amount(value):
    if value is None:
        raise ValueError("Amount is missing.")

    value = str(value).strip()

    if not value:
        raise ValueError("Amount is missing.")

    # Remove leading currency symbols.
    while value and (
        value[0].isspace()
        or unicodedata.category(value[0]) == "Sc"
    ):
        value = value[1:]

    # Remove common currency prefixes.
    value = re.sub(
        r"^(ZAR|R|USD|EUR|GBP)\s*",
        "",
        value,
        flags=re.IGNORECASE,
    )

    # Remove spaces used as thousands separators.
    value = "".join(value.split())

    # Handle formats containing both comma and dot.
    if "," in value and "." in value:
        if value.rfind(",") > value.rfind("."):
            value = value.replace(".", "")
            value = value.replace(",", ".")
        else:
            value = value.replace(",", "")

    # A comma without a dot is treated as a decimal separator.
    elif "," in value:
        value = value.replace(",", ".")

    try:
        return Decimal(value)
    except InvalidOperation:
        raise ValueError("Invalid transaction amount.")
def parse_transaction_date(value):
    value = value.strip()
    parsed_date = None

    try:
        parsed_date = datetime.fromisoformat(
            value.replace("Z", "+00:00")
        )
    except ValueError:
        pass

    if parsed_date is None:
        for date_format in (
            "%Y-%m-%d %H:%M:%S",
            "%Y-%m-%d",
            "%d/%m/%Y %H:%M:%S",
            "%d/%m/%Y",
        ):
            try:
                parsed_date = datetime.strptime(
                    value,
                    date_format,
                )
                break
            except ValueError:
                continue

    if parsed_date is None:
        raise ValueError("Invalid transaction date.")

    if parsed_date.tzinfo is None:
        parsed_date = parsed_date.replace(
            tzinfo=JOHANNESBURG_TZ
        )
    else:
        parsed_date = parsed_date.astimezone(
            JOHANNESBURG_TZ
        )

    return parsed_date.isoformat()
def normalize_status(value):
    normalized = value.strip().upper()

    aliases = {
        "COMPLETED": "SUCCESS",
        "PAID": "SUCCESS",
        "SUCCESSFUL": "SUCCESS",
        "DECLINED": "FAILED",
        "CANCELLED": "FAILED",
        "CANCELED": "FAILED",
    }

    normalized = aliases.get(normalized, normalized)

    if normalized not in VALID_STATUSES:
        raise ValueError("Unsupported transaction status.")

    return normalized


def normalize_payment_method(value):
    normalized = value.strip().upper().replace(" ", "_")
    normalized = normalized.replace("-", "_")

    aliases = {
        "CARD": "CHIP",
        "CREDIT_CARD": "CHIP",
        "DEBIT_CARD": "CHIP",
        "CONTACTLESS": "TAP",
        "NFC": "TAP",
        "QR_CODE": "QR",
        "MOBILE_QR": "QR",
    }

    normalized = aliases.get(normalized, normalized)

    if normalized not in VALID_PAYMENT_METHODS:
        raise ValueError("Unsupported payment method.")

    return normalized


def find_column(headers, field):
    available_headers = {
        normalize_header(header): header
        for header in headers
    }

    for alias in HEADER_ALIASES[field]:
        normalized_alias = normalize_header(alias)

        if normalized_alias in available_headers:
            return available_headers[normalized_alias]

    raise ValueError(
        f"CSV is missing a column for {field}."
    )


def import_pos_csv(file_content, provider, business_id):
    provider = provider.strip().upper()

    if provider not in SUPPORTED_PROVIDERS:
        raise ValueError(
            "Provider must be YOCO, SNAPSCAN or ZAPPER."
        )

    if business_id <= 0:
        raise ValueError("Business ID must be greater than zero.")

    if isinstance(file_content, bytes):
        file_content = file_content.decode("utf-8-sig")

    if not isinstance(file_content, str) or not file_content.strip():
        raise ValueError("The CSV file is empty.")

    reader = csv.DictReader(io.StringIO(file_content))

    if not reader.fieldnames:
        raise ValueError("The CSV file has no header row.")

    columns = {}

    for field in HEADER_ALIASES:
        if field == "paymentMethod" and provider in {"SNAPSCAN", "ZAPPER"}:
            try:
                columns[field] = find_column(reader.fieldnames, field)
            except ValueError:
                columns[field] = None
        else:
            columns[field] = find_column(reader.fieldnames, field)

    imported_transactions = []
    row_errors = []
    seen_transaction_ids = set()
    total_rows = 0

    for row_number, row in enumerate(reader, start=2):
        total_rows += 1

        if total_rows > 10000:
            raise ValueError("CSV cannot contain more than 10,000 rows.")

        try:
            transaction_id = row[columns["providerTransactionId"]].strip()

            if not transaction_id:
                raise ValueError("Transaction ID is missing.")

            if transaction_id in seen_transaction_ids:
                raise ValueError("Duplicate transaction ID")

            seen_transaction_ids.add(transaction_id)
            amount = parse_amount(
                row[columns["amount"]]
            )

            if not amount.is_finite() or amount <= 0:
                raise ValueError("Amount must be greater than zero.")

            status = normalize_status(
                row[columns["status"]]
            )

            if columns["paymentMethod"] is None:
                payment_method = "QR"
            else:
                payment_method = normalize_payment_method(
                    row[columns["paymentMethod"]]
                )
            created_at = parse_transaction_date(
                row[columns["createdAt"]]
            )

            imported_transactions.append(
                {
                    "businessId": business_id,
                    "provider": provider,
                    "providerTransactionId": transaction_id,
                    "amount": str(amount),
                    "paymentMethod": payment_method,
                    "status": status,
                    "createdAt": created_at,
                }
            )
            seen_transaction_ids.add(transaction_id)
        except (ValueError, InvalidOperation, KeyError, AttributeError) as error:
            row_errors.append(
                {
                    "row": row_number,
                    "error": str(error),
                }
            )

    return {
        "provider": provider,
        "businessId": business_id,
        "totalRows": total_rows,
        "importedCount": len(imported_transactions),
        "invalidCount": len(row_errors),
        "transactions": imported_transactions,
        "rowErrors": row_errors,
    }