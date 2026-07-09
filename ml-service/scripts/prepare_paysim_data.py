"""
Loads the raw PaySim dataset, maps it into the platform schema, and writes the processed
result to disk for inspection - a debugging aid, not required for training or scoring.

Usage (from ml-service/):
    python scripts/prepare_paysim_data.py
    python scripts/prepare_paysim_data.py --output ../data/processed/paysim_processed.csv
"""

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.config import settings  # noqa: E402
from app.paysim_loader import load_paysim_raw  # noqa: E402
from app.preprocessing import map_paysim_to_platform_schema  # noqa: E402


def main() -> None:
    parser = argparse.ArgumentParser(description="Map raw PaySim data into the platform schema")
    parser.add_argument("--output", type=str, default=None)
    args = parser.parse_args()

    output_path = (
        Path(args.output) if args.output else settings.paysim_data_path_resolved.parent / "processed" / "paysim_processed.csv"
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)

    df_raw = load_paysim_raw(settings.paysim_data_path_resolved)
    df = map_paysim_to_platform_schema(df_raw)
    df.to_csv(output_path, index=False)
    print(f"Mapped {len(df)} rows -> {output_path}")


if __name__ == "__main__":
    main()
