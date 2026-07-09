import pandas as pd

from app.preprocessing import map_paysim_to_platform_schema


def _raw_paysim_row(is_fraud: int = 0) -> dict:
    return {
        "step": 5,
        "type": "TRANSFER",
        "amount": 1000.0,
        "nameOrig": "C12345678",
        "oldbalanceOrg": 5000.0,
        "newbalanceOrig": 4000.0,
        "nameDest": "M87654321",
        "oldbalanceDest": 100.0,
        "newbalanceDest": 1100.0,
        "isFraud": is_fraud,
        "isFlaggedFraud": 0,
    }


def test_maps_paysim_columns_to_platform_schema():
    df_raw = pd.DataFrame([_raw_paysim_row(), _raw_paysim_row(is_fraud=1)])

    df = map_paysim_to_platform_schema(df_raw)

    assert df.loc[0, "transaction_type"] == "TRANSFER"
    assert df.loc[0, "source_old_balance"] == 5000.0
    assert df.loc[0, "source_new_balance"] == 4000.0
    assert df.loc[0, "destination_old_balance"] == 100.0
    assert df.loc[0, "destination_new_balance"] == 1100.0
    assert df.loc[0, "fraud_label"] == 0
    assert df.loc[1, "fraud_label"] == 1
    assert df.loc[0, "hour_of_day"] == 5 % 24


def test_generates_synthetic_enterprise_fields():
    df_raw = pd.DataFrame([_raw_paysim_row() for _ in range(20)])

    df = map_paysim_to_platform_schema(df_raw)

    for column in [
        "transaction_id", "customer_id", "device_id", "ip_address", "country",
        "merchant_category", "channel", "status", "created_at",
        "transaction_frequency", "failed_attempt_count", "country_risk_score",
        "merchant_risk_score", "new_device", "new_country", "customer_average_amount_ratio",
    ]:
        assert column in df.columns

    assert df["transaction_id"].is_unique
    assert df["country_risk_score"].between(0, 100).all()
    assert df["merchant_risk_score"].between(0, 100).all()
