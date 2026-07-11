"""
Compares each feature's average value across recent predictions against the baseline average
captured at training time (model_manager.metadata["featureBaselineAverages"], written by
train_pipeline). This is intentionally simple - a mean-shift check, not a statistical distribution
test - since the goal here is a readable "is production drifting from training data" signal for a
dashboard, not a rigorous drift test.
"""

from typing import List

from app.model import ModelManager
from app.schemas import FeatureDriftResult, MlPredictionLog

WARNING_THRESHOLD = 0.2
DRIFTED_THRESHOLD = 0.5


def compute_drift(model_manager: ModelManager, recent_predictions: List[MlPredictionLog]) -> List[FeatureDriftResult]:
    baseline_averages: dict = model_manager.metadata.get("featureBaselineAverages") or {}
    if not baseline_averages or not recent_predictions:
        return []

    results: List[FeatureDriftResult] = []
    for feature_name, baseline_average in baseline_averages.items():
        values = [
            p.featureValues[feature_name]
            for p in recent_predictions
            if feature_name in p.featureValues
        ]
        if not values:
            continue
        current_average = sum(values) / len(values)
        denominator = abs(baseline_average) if abs(baseline_average) > 1e-6 else 1.0
        drift_score = abs(current_average - baseline_average) / denominator

        if drift_score >= DRIFTED_THRESHOLD:
            status = "DRIFTED"
        elif drift_score >= WARNING_THRESHOLD:
            status = "WARNING"
        else:
            status = "STABLE"

        results.append(FeatureDriftResult(
            featureName=feature_name,
            baselineAverage=baseline_average,
            currentAverage=current_average,
            driftScore=drift_score,
            driftStatus=status,
        ))
    return results
