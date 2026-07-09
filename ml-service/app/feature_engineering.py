"""
Turns a transaction (single request or a whole PaySim-derived DataFrame) into the fixed-order
numeric feature vector the model is trained on. The same FEATURE_ORDER is used at training time
(build_feature_matrix) and at inference time (build_feature_vector) so the two never drift apart.
"""

from typing import Any, List

import numpy as np
import pandas as pd

FEATURE_ORDER: List[str] = [
    "amount",
    "hour_of_day",
    "transaction_frequency",
    "failed_attempt_count",
    "country_risk_score",
    "new_device",
    "new_country",
    "merchant_risk_score",
    "customer_average_amount_ratio",
    "source_old_balance",
    "source_new_balance",
    "destination_old_balance",
    "destination_new_balance",
    "balance_delta",
    "transaction_type_encoded",
    "flagged_fraud",
]

TRANSACTION_TYPE_ENCODING = {
    "PAYMENT": 0,
    "TRANSFER": 1,
    "CASH_OUT": 2,
    "CASH_IN": 3,
    "DEBIT": 4,
    "WITHDRAWAL": 5,
    "DEPOSIT": 6,
    "CARD_PAYMENT": 7,
}
UNKNOWN_TRANSACTION_TYPE_ENCODING = -1


def encode_transaction_type(value: Any) -> int:
    if value is None:
        return UNKNOWN_TRANSACTION_TYPE_ENCODING
    return TRANSACTION_TYPE_ENCODING.get(str(value).upper(), UNKNOWN_TRANSACTION_TYPE_ENCODING)


def _balance_delta(source_old_balance: float, source_new_balance: float, amount: float) -> float:
    """Discrepancy between the expected debit (amount) and the actual source balance change -
    a strong PaySim fraud signal (large discrepancies mean the balances don't add up)."""
    return (source_old_balance - source_new_balance) - amount


def build_feature_dict(request: Any) -> dict:
    """Accepts a TransactionScoreRequest (or anything with the same attributes)."""
    amount = float(request.amount)
    source_old_balance = float(request.sourceOldBalance)
    source_new_balance = float(request.sourceNewBalance)

    return {
        "amount": amount,
        "hour_of_day": float(request.hourOfDay),
        "transaction_frequency": float(request.transactionFrequency),
        "failed_attempt_count": float(request.failedAttemptCount),
        "country_risk_score": float(request.countryRiskScore),
        "new_device": float(bool(request.newDevice)),
        "new_country": float(bool(request.newCountry)),
        "merchant_risk_score": float(request.merchantRiskScore),
        "customer_average_amount_ratio": float(request.customerAverageAmountRatio),
        "source_old_balance": source_old_balance,
        "source_new_balance": source_new_balance,
        "destination_old_balance": float(request.destinationOldBalance),
        "destination_new_balance": float(request.destinationNewBalance),
        "balance_delta": _balance_delta(source_old_balance, source_new_balance, amount),
        "transaction_type_encoded": float(encode_transaction_type(request.transactionType)),
        "flagged_fraud": float(bool(request.flaggedFraud)),
    }


def build_feature_vector(request: Any) -> List[float]:
    features = build_feature_dict(request)
    return [features[name] for name in FEATURE_ORDER]


def build_feature_matrix(df: pd.DataFrame) -> np.ndarray:
    """Vectorized equivalent of build_feature_vector, used at training time. Expects df to
    already be in platform schema (see preprocessing.map_paysim_to_platform_schema)."""
    out = pd.DataFrame(index=df.index)
    out["amount"] = df["amount"].astype(float)
    out["hour_of_day"] = df["hour_of_day"].astype(float)
    out["transaction_frequency"] = df["transaction_frequency"].astype(float)
    out["failed_attempt_count"] = df["failed_attempt_count"].astype(float)
    out["country_risk_score"] = df["country_risk_score"].astype(float)
    out["new_device"] = df["new_device"].astype(float)
    out["new_country"] = df["new_country"].astype(float)
    out["merchant_risk_score"] = df["merchant_risk_score"].astype(float)
    out["customer_average_amount_ratio"] = df["customer_average_amount_ratio"].astype(float)
    out["source_old_balance"] = df["source_old_balance"].astype(float)
    out["source_new_balance"] = df["source_new_balance"].astype(float)
    out["destination_old_balance"] = df["destination_old_balance"].astype(float)
    out["destination_new_balance"] = df["destination_new_balance"].astype(float)
    out["balance_delta"] = _balance_delta(
        out["source_old_balance"], out["source_new_balance"], out["amount"]
    )
    out["transaction_type_encoded"] = df["transaction_type"].map(encode_transaction_type).astype(float)
    out["flagged_fraud"] = df["flagged_fraud"].astype(float)

    return out[FEATURE_ORDER].to_numpy(dtype=float)
