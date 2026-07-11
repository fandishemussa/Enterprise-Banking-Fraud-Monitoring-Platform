"""
Appends one JSON line per /api/v1/score (and batch-score) call to logs/predictions.jsonl - the
lightweight "database" model-monitoring reads from, matching this service's existing no-DB
convention (see paysim_loader for the equivalent input-side pattern). Never raises: a monitoring
write failure must not break actual fraud scoring, the same reasoning MlScoringClient.score() uses
on the Spring Boot side for its own failure handling.
"""

import json
import logging
import uuid
from pathlib import Path
from typing import Iterator

from app.feature_engineering import build_feature_dict
from app.schemas import MlPredictionLog, TransactionScoreRequest, TransactionScoreResponse
from app.utils import utc_now

logger = logging.getLogger(__name__)


def log_prediction(
    log_path: Path,
    request: TransactionScoreRequest,
    response: TransactionScoreResponse,
) -> None:
    try:
        log_path.parent.mkdir(parents=True, exist_ok=True)
        entry = MlPredictionLog(
            predictionId=str(uuid.uuid4()),
            transactionId=response.transactionId,
            mlScore=response.mlScore,
            riskLevel=response.riskLevel,
            isAnomaly=response.isAnomaly,
            fallbackMode=response.fallbackMode,
            modelVersion=response.modelVersion,
            featureValues=build_feature_dict(request),
            createdAt=utc_now(),
        )
        with log_path.open("a", encoding="utf-8") as f:
            f.write(entry.model_dump_json() + "\n")
    except Exception:
        logger.exception("Failed to write prediction log entry for transaction %s", response.transactionId)


def read_predictions(log_path: Path, limit: int = 0) -> list[MlPredictionLog]:
    """Returns predictions oldest-first. limit=0 means "all"; otherwise the most recent `limit`."""
    entries = list(_iter_predictions(log_path))
    if limit and limit > 0:
        entries = entries[-limit:]
    return entries


def _iter_predictions(log_path: Path) -> Iterator[MlPredictionLog]:
    if not log_path.exists():
        return
    with log_path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                yield MlPredictionLog.model_validate_json(line)
            except Exception:
                logger.warning("Skipping malformed prediction log line")
