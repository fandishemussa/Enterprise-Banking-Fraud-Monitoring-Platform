"""
Loads the raw PaySim CSV. PAYSIM_DATA_PATH may point at either a directory (the first *.csv
found inside it is used) or directly at a single CSV file, so users can either drop the Kaggle
file into data/raw/paysim/ or point the env var straight at wherever they already have it.
"""

from pathlib import Path
from typing import Optional

import pandas as pd

REQUIRED_PAYSIM_COLUMNS = [
    "step",
    "type",
    "amount",
    "nameOrig",
    "oldbalanceOrg",
    "newbalanceOrig",
    "nameDest",
    "oldbalanceDest",
    "newbalanceDest",
    "isFraud",
    "isFlaggedFraud",
]


def resolve_dataset_file(path: Path) -> Optional[Path]:
    if path.is_file() and path.suffix.lower() == ".csv":
        return path
    if path.is_dir():
        csv_files = sorted(path.glob("*.csv"))
        return csv_files[0] if csv_files else None
    return None


def dataset_available(path: Path) -> bool:
    return resolve_dataset_file(path) is not None


def load_paysim_raw(path: Path, max_rows: Optional[int] = None) -> pd.DataFrame:
    dataset_file = resolve_dataset_file(path)
    if dataset_file is None:
        raise FileNotFoundError(
            f"PaySim dataset not found at '{path}'. Place the dataset in data/raw/paysim/ "
            "(or set PAYSIM_DATA_PATH) before training."
        )

    # nrows avoids reading the full file into memory when the caller only wants a sample -
    # important for the real Kaggle PaySim export (6M+ rows, ~500MB).
    df = pd.read_csv(dataset_file, nrows=max_rows if max_rows and max_rows > 0 else None)
    missing_columns = [col for col in REQUIRED_PAYSIM_COLUMNS if col not in df.columns]
    if missing_columns:
        raise ValueError(
            f"'{dataset_file}' is missing expected PaySim columns: {missing_columns}"
        )
    return df
