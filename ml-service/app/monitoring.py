"""
Read-side of model monitoring: aggregates logs/predictions.jsonl (via prediction_logger) and
logs/retraining_history.jsonl (appended to by model.train_pipeline) into the summary/distribution/
retraining-history responses main.py serves. Doesn't touch scoring at all - purely observability
over what scoring has already produced.
"""

import json
import logging
from pathlib import Path
from typing import List

from app.model import ModelManager
from app.prediction_logger import read_predictions
from app.schemas import MonitoringSummaryResponse, RetrainingHistoryEntry, ScoreDistribution
from app.utils import risk_level_from_score

logger = logging.getLogger(__name__)


def get_summary(
    predictions_log_path: Path,
    model_manager: ModelManager,
    dataset_available: bool,
) -> MonitoringSummaryResponse:
    predictions = read_predictions(predictions_log_path)
    total = len(predictions)
    fallback_count = sum(1 for p in predictions if p.fallbackMode)
    anomaly_count = sum(1 for p in predictions if p.isAnomaly)
    high_count = sum(1 for p in predictions if p.riskLevel == "HIGH")
    critical_count = sum(1 for p in predictions if p.riskLevel == "CRITICAL")
    average_score = (sum(p.mlScore for p in predictions) / total) if total else 0.0

    metadata = model_manager.metadata or {}
    return MonitoringSummaryResponse(
        totalPredictions=total,
        averageMlScore=average_score,
        highRiskPredictionCount=high_count,
        criticalRiskPredictionCount=critical_count,
        anomalyCount=anomaly_count,
        fallbackModeCount=fallback_count,
        fallbackRate=(fallback_count / total * 100) if total else 0.0,
        modelVersion=metadata.get("modelVersion"),
        lastTrainingDate=metadata.get("trainingDate"),
        datasetAvailable=dataset_available,
        modelLoaded=model_manager.is_loaded(),
    )


def get_score_distribution(predictions_log_path: Path) -> ScoreDistribution:
    predictions = read_predictions(predictions_log_path)
    counts = {"LOW": 0, "MEDIUM": 0, "HIGH": 0, "CRITICAL": 0}
    for p in predictions:
        level = p.riskLevel if p.riskLevel in counts else risk_level_from_score(p.mlScore)
        counts[level] += 1
    return ScoreDistribution(low=counts["LOW"], medium=counts["MEDIUM"], high=counts["HIGH"], critical=counts["CRITICAL"])


def get_retraining_history(history_log_path: Path) -> List[RetrainingHistoryEntry]:
    if not history_log_path.exists():
        return []
    entries: List[RetrainingHistoryEntry] = []
    with history_log_path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                entries.append(RetrainingHistoryEntry.model_validate_json(line))
            except Exception:
                logger.warning("Skipping malformed retraining history line")
    return entries


def append_retraining_history(history_log_path: Path, metadata: dict) -> None:
    try:
        history_log_path.parent.mkdir(parents=True, exist_ok=True)
        entry = RetrainingHistoryEntry(
            modelVersion=metadata["modelVersion"],
            trainingDate=metadata["trainingDate"],
            datasetName=metadata["datasetName"],
            numberOfTrainingRows=metadata["numberOfTrainingRows"],
            status="SUCCESS",
            metrics={
                "decisionScoreMin": metadata.get("decisionScoreMin"),
                "decisionScoreMax": metadata.get("decisionScoreMax"),
            },
        )
        with history_log_path.open("a", encoding="utf-8") as f:
            f.write(entry.model_dump_json() + "\n")
    except Exception:
        logger.exception("Failed to append retraining history entry")
