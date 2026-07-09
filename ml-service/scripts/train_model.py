"""
Trains (or retrains) the Isolation Forest fraud model from the PaySim dataset and saves the
artifact + metadata to models/. This is the CLI equivalent of POST /api/v1/retrain.

Usage (from ml-service/):
    python scripts/train_model.py
"""

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.config import settings  # noqa: E402
from app.model import ModelManager, train_pipeline  # noqa: E402
from app.paysim_loader import dataset_available  # noqa: E402


def main() -> None:
    parser = argparse.ArgumentParser(description="Train the Isolation Forest fraud model from PaySim")
    parser.add_argument(
        "--rows", type=int, default=settings.train_sample_rows,
        help=f"Rows to read from the dataset (default {settings.train_sample_rows:,}; use 0 for all rows - "
             "the full Kaggle PaySim export is 6M+ rows and can exceed available RAM on a dev machine)",
    )
    args = parser.parse_args()

    if not dataset_available(settings.paysim_data_path_resolved):
        print(
            f"PaySim dataset not found at {settings.paysim_data_path_resolved}. "
            "Place the dataset there (or set PAYSIM_DATA_PATH) before training. "
            "You can generate a sample file with scripts/generate_sample_data.py."
        )
        sys.exit(1)

    model_manager = ModelManager(settings.model_path_resolved, settings.model_metadata_path_resolved)
    metadata = train_pipeline(
        settings.paysim_data_path_resolved,
        model_manager,
        settings.ml_service_version,
        settings.model_path_resolved,
        max_rows=args.rows,
    )

    print(f"Trained on {metadata['numberOfTrainingRows']} rows")
    print(f"Model saved to {metadata['modelPath']}")
    print(f"Metadata saved to {settings.model_metadata_path_resolved}")


if __name__ == "__main__":
    main()
