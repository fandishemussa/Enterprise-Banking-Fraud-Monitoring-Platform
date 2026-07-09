import pandas as pd

from app.feature_engineering import (
    FEATURE_ORDER,
    build_feature_matrix,
    build_feature_vector,
    encode_transaction_type,
)
from app.schemas import TransactionScoreRequest


def test_feature_order_has_sixteen_features():
    assert len(FEATURE_ORDER) == 16
    assert FEATURE_ORDER[0] == "amount"
    assert FEATURE_ORDER[-1] == "flagged_fraud"


def test_build_feature_vector_matches_feature_order_length():
    request = TransactionScoreRequest(
        transactionId="TXN-1",
        amount=100.0,
        transactionType="TRANSFER",
        hourOfDay=10,
    )

    vector = build_feature_vector(request)

    assert len(vector) == len(FEATURE_ORDER)
    assert all(isinstance(value, float) for value in vector)


def test_encode_transaction_type_known_and_unknown():
    assert encode_transaction_type("TRANSFER") != encode_transaction_type("PAYMENT")
    assert encode_transaction_type("SOMETHING_UNKNOWN") == -1


def test_build_feature_matrix_matches_feature_order_columns():
    df = pd.DataFrame({
        "amount": [100.0],
        "hour_of_day": [10],
        "transaction_frequency": [1],
        "failed_attempt_count": [0],
        "country_risk_score": [10],
        "new_device": [0],
        "new_country": [0],
        "merchant_risk_score": [10],
        "customer_average_amount_ratio": [1.0],
        "source_old_balance": [1000.0],
        "source_new_balance": [900.0],
        "destination_old_balance": [0.0],
        "destination_new_balance": [100.0],
        "transaction_type": ["PAYMENT"],
        "flagged_fraud": [0],
    })

    matrix = build_feature_matrix(df)

    assert matrix.shape == (1, len(FEATURE_ORDER))
