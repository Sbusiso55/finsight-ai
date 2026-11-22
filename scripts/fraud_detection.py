from datetime import datetime, time
from statistics import median
from collections import defaultdict
LARGE_PAYMENT_MULTIPLIER = 3
MINIMUM_LARGE_PAYMENT = 5000

RAPID_PAYMENT_WINDOW_SECONDS = 10
RAPID_PAYMENT_COUNT = 5

UNUSUAL_HOUR_START = 0
UNUSUAL_HOUR_END = 5
def parse_transaction_time(value):
    """Convert a transaction timestamp into a datetime object."""

    if isinstance(value, datetime):
        return value

    if isinstance(value, str):
        return datetime.fromisoformat(
            value.replace("Z", "+00:00")
        )

    raise ValueError("Invalid transaction timestamp.")
def detect_payment_fraud(transactions):
    """
    Analyze payment transactions and identify suspicious activity.
    """

    if not isinstance(transactions, list) or not transactions:
        raise ValueError("At least one transaction is required.")

    business_transactions = defaultdict(list)

    for transaction in transactions:
        required_fields = [
            "id",
            "businessId",
            "provider",
            "providerTransactionId",
            "amount",
            "paymentMethod",
            "status",
            "createdAt",
        ]

        for field in required_fields:
            if field not in transaction:
                raise ValueError(f"Missing transaction field: {field}")

        if transaction["status"] != "SUCCESS":
            continue

        amount = float(transaction["amount"])

        if amount <= 0:
            raise ValueError("Transaction amounts must be greater than zero.")

        created_at = parse_transaction_time(
            transaction["createdAt"]
        )

        business_transactions[transaction["businessId"]].append(
            {
                **transaction,
                "_amount": amount,
                "_created_at": created_at,
            }
        )

    suspicious_transactions = []

    for business_id, payments in business_transactions.items():
        typical_amount = median(
            payment["_amount"] for payment in payments
        )

        for payment in payments:
            reasons = []

            amount = payment["_amount"]
            transaction_time = payment["_created_at"]

            # Rule 1: Unusually large payments
            if (
                amount >= MINIMUM_LARGE_PAYMENT
                and amount >= typical_amount * LARGE_PAYMENT_MULTIPLIER
            ):
                reasons.append("Unusually large payment")

            # Rule 2: Payments during unusual hours
            if (
                UNUSUAL_HOUR_START
                <= transaction_time.hour
                < UNUSUAL_HOUR_END
            ):
                reasons.append("Payment made during unusual hours")

            # Rule 3: Rapid payment activity
            rapid_payment_count = sum(
                1
                for other in payments
                if 0 <= (
                        other["_created_at"] - transaction_time
                ).total_seconds() <= RAPID_PAYMENT_WINDOW_SECONDS
            )

            if rapid_payment_count >= RAPID_PAYMENT_COUNT:
                reasons.append("Rapid burst of payments")

            if reasons:
                suspicious_transactions.append(
                    {
                        "transactionId": payment["id"],
                        "businessId": business_id,
                        "amount": amount,
                        "riskLevel": (
                            "HIGH" if len(reasons) >= 2 else "MEDIUM"
                        ),
                        "reasons": reasons,
                    }
                )

    return {
        "totalTransactions": len(transactions),
        "suspiciousCount": len(suspicious_transactions),
        "suspiciousTransactions": suspicious_transactions,
    }