"""
Owns the trained Isolation Forest artifact: loading it at startup, predicting for a single
feature vector, and (re)training it from a PaySim dataset. Isolation Forest's decision_function
has no fixed range, so the min/max seen during training is stored in the metadata and used to
normalize future predictions into a 0-100 risk score.
"""

import json
import logging
import time
from pathlib import Path
from typing import Callable, Optional, Tuple

import joblib
import numpy as np
from sklearn.ensemble import IsolationForest

from app import feature_engineering, paysim_loader, preprocessing
from app.utils import utc_now

logger = logging.getLogger(__name__)

ProgressCallback = Callable[[str], None]


def _default_progress(message: str) -> None:
    print(message, flush=True)
    logger.info(message)

DEFAULT_DECISION_SCORE_MIN = -0.5
DEFAULT_DECISION_SCORE_MAX = 0.5


class ModelManager:
    def __init__(self, model_path: Path, metadata_path: Path):
        self.model_path = model_path
        self.metadata_path = metadata_path
        self.model = None
        self.metadata: dict = {}

    def load(self) -> bool:
        if not self.model_path.exists():
            self.model = None
            self.metadata = {}
            return False
        try:
            self.model = joblib.load(self.model_path)
            self.metadata = (
                json.loads(self.metadata_path.read_text()) if self.metadata_path.exists() else {}
            )
            return True
        except Exception:
            logger.exception("Failed to load model from %s", self.model_path)
            self.model = None
            self.metadata = {}
            return False

    def is_loaded(self) -> bool:
        return self.model is not None

    def save(self, model, metadata: dict) -> None:
        self.model_path.parent.mkdir(parents=True, exist_ok=True)
        joblib.dump(model, self.model_path)
        self.metadata_path.write_text(json.dumps(metadata, indent=2, default=str))
        self.model = model
        self.metadata = metadata

    def predict(self, feature_vector: list) -> Tuple[bool, int]:
        if self.model is None:
            raise RuntimeError("Model is not loaded")

        features = np.array(feature_vector, dtype=float).reshape(1, -1)
        raw_score = float(self.model.decision_function(features)[0])

        score_min = float(self.metadata.get("decisionScoreMin", DEFAULT_DECISION_SCORE_MIN))
        score_max = float(self.metadata.get("decisionScoreMax", DEFAULT_DECISION_SCORE_MAX))

        if score_max <= score_min:
            normalized = 0.5
        else:
            # decision_function: higher = more normal, lower = more anomalous.
            normalized = (score_max - raw_score) / (score_max - score_min)

        ml_score = int(np.clip(round(normalized * 100), 0, 100))
        is_anomaly = ml_score >= 61
        return is_anomaly, ml_score

    def get_info(self) -> dict:
        return dict(self.metadata)


def train_pipeline(
    paysim_path: Path,
    model_manager: ModelManager,
    model_version: str,
    model_path: Path,
    progress_callback: Optional[ProgressCallback] = None,
    max_rows: Optional[int] = None,
) -> dict:
    report = progress_callback or _default_progress
    started_at = time.perf_counter()

    def _step(label: str, since: float) -> float:
        now = time.perf_counter()
        report(f"[{now - started_at:6.1f}s] {label} ({now - since:.1f}s)")
        return now

    row_note = f"first {max_rows:,} rows" if max_rows and max_rows > 0 else "all rows"
    report(f"[  0.0s] Loading PaySim dataset from {paysim_path} ({row_note}) ...")
    t = time.perf_counter()
    df_raw = paysim_loader.load_paysim_raw(paysim_path, max_rows=max_rows)
    t = _step(f"Loaded {len(df_raw):,} raw rows", t)

    df = preprocessing.map_paysim_to_platform_schema(df_raw)
    t = _step(f"Mapped {len(df):,} rows to platform schema", t)

    feature_matrix = feature_engineering.build_feature_matrix(df)
    t = _step(f"Built feature matrix {feature_matrix.shape}", t)

    report(f"[{time.perf_counter() - started_at:6.1f}s] Training IsolationForest on "
           f"{feature_matrix.shape[0]:,} rows x {feature_matrix.shape[1]} features ...")
    model = IsolationForest(n_estimators=200, contamination="auto", random_state=42, n_jobs=-1)
    model.fit(feature_matrix)
    t = _step("Trained IsolationForest", t)

    decision_scores = model.decision_function(feature_matrix)
    t = _step("Computed decision scores for score normalization", t)

    # Per-feature training-time averages, used as the baseline model-monitoring/drift_detection
    # compares live prediction feature values against (see docs/model-monitoring.md).
    feature_baseline_averages = {
        name: float(feature_matrix[:, i].mean())
        for i, name in enumerate(feature_engineering.FEATURE_ORDER)
    }

    metadata = {
        "modelName": "PaySim Isolation Forest Fraud Detector",
        "modelType": "IsolationForest",
        "modelVersion": model_version,
        "trainingDate": utc_now().isoformat(),
        "datasetName": "PaySim Synthetic Financial Transactions",
        "numberOfTrainingRows": int(len(df)),
        "featureList": feature_engineering.FEATURE_ORDER,
        "modelPath": str(model_path),
        "decisionScoreMin": float(decision_scores.min()),
        "decisionScoreMax": float(decision_scores.max()),
        "featureBaselineAverages": feature_baseline_averages,
    }

    model_manager.save(model, metadata)
    _step(f"Saved model to {model_path}", t)
    report(f"[{time.perf_counter() - started_at:6.1f}s] Done.")

    return metadata
