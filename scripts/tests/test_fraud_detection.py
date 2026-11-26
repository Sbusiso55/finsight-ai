from datetime import datetime, timedelta

from scripts.fraud_detection import detect_payment_fraud


def make_transaction(
    transaction_id,
    amount=1000,
    created_at="2026-11-20T10:00:00",
    status="SUCCESS",
):
    return {
        "id": transaction_id,
        "businessId": 10,
        "provider": "YOCO",
        "providerTransactionId": f"TX-{transaction_id}",
        "amount": amount,
        "paymentMethod": "CARD",
        "status": status,
        "createdAt": created_at,
    }


def test_large_payment_is_flagged():
    transactions = [
        make_transaction("1", 1000),
        make_transaction("2", 1000),
        make_transaction("3", 1000),
        make_transaction("4", 6000),
    ]

    result = detect_payment_fraud(transactions)

    assert result["suspiciousCount"] == 1
    assert result["suspiciousTransactions"][0]["transactionId"] == "4"
    assert "Unusually large payment" in result["suspiciousTransactions"][0]["reasons"]


def test_payment_at_0200_is_flagged():
    transaction = make_transaction(
        "1",
        created_at="2026-11-20T02:00:00",
    )

    result = detect_payment_fraud([transaction])

    assert result["suspiciousCount"] == 1
    assert "Payment made during unusual hours" in result["suspiciousTransactions"][0]["reasons"]


def test_five_payments_within_ten_seconds_are_flagged():
    start_time = datetime.fromisoformat("2026-11-20T10:00:00")

    transactions = [
        make_transaction(
            str(i),
            created_at=(start_time + timedelta(seconds=i * 2)).isoformat(),
        )
        for i in range(5)
    ]

    result = detect_payment_fraud(transactions)

    assert result["suspiciousCount"] > 0

    first_payment = next(
        payment
        for payment in result["suspiciousTransactions"]
        if payment["transactionId"] == "0"
    )

    assert "Rapid burst of payments" in first_payment["reasons"]


def test_failed_payments_are_ignored():
    transaction = make_transaction(
        "1",
        amount=10000,
        created_at="2026-11-20T02:00:00",
        status="FAILED",
    )

    result = detect_payment_fraud([transaction])

    assert result["totalTransactions"] == 1
    assert result["suspiciousCount"] == 0
    assert result["suspiciousTransactions"] == []


def test_two_fraud_reasons_produce_high_risk():
    transactions = [
        make_transaction("1", 1000),
        make_transaction("2", 1000),
        make_transaction("3", 1000),
        make_transaction(
            "4",
            amount=6000,
            created_at="2026-11-20T02:00:00",
        ),
    ]

    result = detect_payment_fraud(transactions)

    suspicious_payment = next(
        payment
        for payment in result["suspiciousTransactions"]
        if payment["transactionId"] == "4"
    )

    assert len(suspicious_payment["reasons"]) >= 2
    assert suspicious_payment["riskLevel"] == "HIGH"