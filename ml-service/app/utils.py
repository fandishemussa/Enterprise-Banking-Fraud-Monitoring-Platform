from datetime import datetime, timezone


def risk_level_from_score(score: int) -> str:
    if score >= 81:
        return "CRITICAL"
    if score >= 61:
        return "HIGH"
    if score >= 31:
        return "MEDIUM"
    return "LOW"


def utc_now() -> datetime:
    return datetime.now(timezone.utc)
