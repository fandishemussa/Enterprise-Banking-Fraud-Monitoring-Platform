"""
Two scoring paths, both producing a TransactionScoreResponse:

1. ML path (model loaded): Isolation Forest predicts an anomaly score, which becomes mlScore.
2. Fallback path (no model, or prediction throws): a fixed point-rule table produces mlScore.

Both paths reuse the same rule-based reason list for the human-readable `explanation` field -
Isolation Forest doesn't expose per-feature attribution on its own, so the same interpretable
rules used for fallback scoring double as the explanation layer on top of the ML score.
"""

import logging
from typing import List, Tuple

from app.feature_engineering import build_feature_vector
from app.model import ModelManager
from app.schemas import TransactionScoreRequest, TransactionScoreResponse
from app.utils import risk_level_from_score

logger = logging.getLogger(__name__)


def _evaluate_rules(request: TransactionScoreRequest) -> List[Tuple[int, str]]:
    triggered: List[Tuple[int, str]] = []

    if request.amount > 10000:
        triggered.append((40, "Amount exceeds 10,000"))
    elif request.amount > 5000:
        triggered.append((25, "High transaction amount compared to customer average"))

    if 0 <= request.hourOfDay <= 5:
        triggered.append((15, "Transaction occurred at unusual hour"))

    if request.transactionFrequency > 5:
        triggered.append((25, "Unusually high transaction frequency"))

    if request.failedAttemptCount > 2:
        triggered.append((20, "Multiple recent failed attempts"))

    if request.countryRiskScore > 70:
        triggered.append((20, "Transaction from a high-risk country"))

    if request.newDevice:
        triggered.append((20, "New device detected"))

    if request.newCountry:
        triggered.append((25, "New country detected"))

    if request.merchantRiskScore > 70:
        triggered.append((20, "High-risk merchant category"))

    if request.customerAverageAmountRatio > 3:
        triggered.append((25, "Amount significantly exceeds customer's average"))

    return triggered


def build_explanation(request: TransactionScoreRequest) -> List[str]:
    return [reason for _, reason in _evaluate_rules(request)]


def fallback_score(request: TransactionScoreRequest) -> Tuple[int, List[str]]:
    triggered = _evaluate_rules(request)
    total = min(sum(points for points, _ in triggered), 100)
    return total, [reason for _, reason in triggered]


def score_transaction(
    request: TransactionScoreRequest,
    model_manager: ModelManager,
    default_model_version: str,
) -> TransactionScoreResponse:
    if model_manager.is_loaded():
        try:
            feature_vector = build_feature_vector(request)
            is_anomaly, ml_score = model_manager.predict(feature_vector)
            explanation = build_explanation(request) or ["No significant risk indicators detected"]
            return TransactionScoreResponse(
                transactionId=request.transactionId,
                mlScore=ml_score,
                isAnomaly=is_anomaly,
                riskLevel=risk_level_from_score(ml_score),
                explanation=explanation,
                modelVersion=model_manager.metadata.get("modelVersion", default_model_version),
                fallbackMode=False,
            )
        except Exception:
            logger.exception("Model prediction failed for transaction %s, using fallback scoring",
                              request.transactionId)

    score, explanation = fallback_score(request)
    return TransactionScoreResponse(
        transactionId=request.transactionId,
        mlScore=score,
        isAnomaly=score >= 61,
        riskLevel=risk_level_from_score(score),
        explanation=explanation or ["No significant risk indicators detected"],
        modelVersion=default_model_version,
        fallbackMode=True,
    )
