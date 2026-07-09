"""
Maps raw PaySim rows into the platform's enterprise banking transaction schema. PaySim only has
step/type/amount/balances/fraud labels - it has no notion of device, country, merchant risk,
recent frequency, etc. Those enterprise-only fields are synthesized here with a fixed random
seed (reproducible) and mildly correlated with isFraud, so the demo Isolation Forest has a real
signal to separate rather than pure noise.
"""

import numpy as np
import pandas as pd

RANDOM_SEED = 42

COUNTRIES = ["United States", "Mexico", "Nigeria", "United Kingdom", "Germany", "Brazil", "India"]
MERCHANT_CATEGORIES = ["GROCERY", "ELECTRONICS", "TRAVEL", "RETAIL", "CRYPTO", "GAMBLING", "UTILITIES"]
CHANNELS = ["WEB", "MOBILE", "ATM", "POS", "BRANCH"]

BASE_TIMESTAMP = pd.Timestamp("2026-01-01T00:00:00")


def map_paysim_to_platform_schema(df_raw: pd.DataFrame) -> pd.DataFrame:
    rng = np.random.default_rng(RANDOM_SEED)
    n = len(df_raw)

    df = pd.DataFrame(index=df_raw.index)
    df["step"] = df_raw["step"].astype(int)
    df["transaction_type"] = df_raw["type"].astype(str)
    df["amount"] = df_raw["amount"].astype(float)
    df["source_account_id"] = df_raw["nameOrig"].astype(str)
    df["source_old_balance"] = df_raw["oldbalanceOrg"].astype(float)
    df["source_new_balance"] = df_raw["newbalanceOrig"].astype(float)
    df["destination_account_id"] = df_raw["nameDest"].astype(str)
    df["destination_old_balance"] = df_raw["oldbalanceDest"].astype(float)
    df["destination_new_balance"] = df_raw["newbalanceDest"].astype(float)
    df["fraud_label"] = df_raw["isFraud"].astype(int)
    df["flagged_fraud"] = df_raw["isFlaggedFraud"].astype(int)

    # Enterprise identity fields (vectorized with pandas string ops - a plain Python
    # f-string loop over millions of PaySim rows would take far too long)
    df["transaction_id"] = "PAYSIM-TXN-" + pd.Series(np.arange(1, n + 1), index=df.index).astype(str).str.zfill(7)
    df["customer_id"] = df["source_account_id"]
    df["device_id"] = "device-" + pd.Series(rng.integers(1, 500, n), index=df.index).astype(str)

    octets = rng.integers(1, 255, size=(n, 4))
    df["ip_address"] = (
        pd.Series(octets[:, 0], index=df.index).astype(str) + "."
        + pd.Series(octets[:, 1], index=df.index).astype(str) + "."
        + pd.Series(octets[:, 2], index=df.index).astype(str) + "."
        + pd.Series(octets[:, 3], index=df.index).astype(str)
    )
    df["country"] = rng.choice(COUNTRIES, n)
    df["merchant_category"] = rng.choice(MERCHANT_CATEGORIES, n)
    df["channel"] = rng.choice(CHANNELS, n)
    df["status"] = "SUCCESS"
    df["created_at"] = BASE_TIMESTAMP + pd.to_timedelta(df["step"], unit="h")

    # PaySim's step is hours-since-start; PaySim covers ~30 days (steps 1-744)
    df["hour_of_day"] = (df["step"] % 24).astype(int)

    fraud_mask = df["fraud_label"] == 1

    df["transaction_frequency"] = rng.integers(0, 6, n) + fraud_mask.to_numpy() * rng.integers(0, 4, n)
    df["failed_attempt_count"] = rng.integers(0, 3, n) + fraud_mask.to_numpy() * rng.integers(0, 3, n)
    df["country_risk_score"] = np.clip(
        rng.integers(0, 50, n) + fraud_mask.to_numpy() * rng.integers(20, 50, n), 0, 100
    )
    df["merchant_risk_score"] = np.clip(
        rng.integers(0, 50, n) + fraud_mask.to_numpy() * rng.integers(20, 50, n), 0, 100
    )
    df["new_device"] = (rng.random(n) < (0.1 + fraud_mask.to_numpy() * 0.5)).astype(int)
    df["new_country"] = (rng.random(n) < (0.05 + fraud_mask.to_numpy() * 0.4)).astype(int)

    customer_avg_amount = df.groupby("customer_id")["amount"].transform("mean").replace(0, np.nan)
    df["customer_average_amount_ratio"] = (df["amount"] / customer_avg_amount).fillna(1.0)

    return df
