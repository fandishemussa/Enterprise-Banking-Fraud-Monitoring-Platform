"""
Generates a small PaySim-shaped CSV so /api/v1/retrain and scripts/train_model.py can be
exercised end-to-end without downloading the real (multi-GB) Kaggle PaySim dataset.

Usage (from ml-service/):
    python scripts/generate_sample_data.py
    python scripts/generate_sample_data.py --rows 2000 --output ../data/raw/paysim/sample_paysim.csv
"""

import argparse
import sys
from pathlib import Path

import numpy as np
import pandas as pd

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.config import settings  # noqa: E402

TRANSACTION_TYPES = ["PAYMENT", "TRANSFER", "CASH_OUT", "CASH_IN", "DEBIT"]


def generate_paysim_like_data(rows: int, seed: int = 7) -> pd.DataFrame:
    rng = np.random.default_rng(seed)

    step = rng.integers(1, 744, rows)
    tx_type = rng.choice(TRANSACTION_TYPES, rows)
    amount = np.round(rng.exponential(scale=500, size=rows) + 1, 2)

    old_balance_org = np.round(rng.uniform(0, 20000, rows), 2)
    new_balance_orig = np.clip(old_balance_org - amount, 0, None)
    old_balance_dest = np.round(rng.uniform(0, 20000, rows), 2)
    new_balance_dest = old_balance_dest + amount

    is_fraud = (rng.random(rows) < 0.01).astype(int)
    # Fraudulent transactions tend to be larger and drain the source account, per the real PaySim data
    is_fraud_mask = is_fraud == 1
    amount[is_fraud_mask] = amount[is_fraud_mask] * rng.uniform(5, 20, is_fraud_mask.sum())
    new_balance_orig[is_fraud_mask] = 0

    is_flagged_fraud = ((is_fraud == 1) & (amount > 200000)).astype(int)

    return pd.DataFrame({
        "step": step,
        "type": tx_type,
        "amount": amount,
        "nameOrig": [f"C{value}" for value in rng.integers(10_000_000, 99_999_999, rows)],
        "oldbalanceOrg": old_balance_org,
        "newbalanceOrig": new_balance_orig,
        "nameDest": [f"M{value}" for value in rng.integers(10_000_000, 99_999_999, rows)],
        "oldbalanceDest": old_balance_dest,
        "newbalanceDest": new_balance_dest,
        "isFraud": is_fraud,
        "isFlaggedFraud": is_flagged_fraud,
    })


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a PaySim-shaped sample CSV")
    parser.add_argument("--rows", type=int, default=2000)
    parser.add_argument("--output", type=str, default=None)
    args = parser.parse_args()

    output_path = Path(args.output) if args.output else settings.paysim_data_path_resolved / "sample_paysim.csv"
    output_path.parent.mkdir(parents=True, exist_ok=True)

    df = generate_paysim_like_data(args.rows)
    df.to_csv(output_path, index=False)
    print(f"Wrote {len(df)} rows to {output_path}")


if __name__ == "__main__":
    main()
