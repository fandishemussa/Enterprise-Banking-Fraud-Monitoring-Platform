import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import pytest
from fastapi.testclient import TestClient

from app.main import app


@pytest.fixture()
def client() -> TestClient:
    return TestClient(app)


@pytest.fixture()
def sample_transaction() -> dict:
    return {
        "transactionId": "TXN-1001",
        "amount": 2500.75,
        "transactionType": "TRANSFER",
        "hourOfDay": 23,
        "transactionFrequency": 7,
        "failedAttemptCount": 2,
        "countryRiskScore": 60,
        "newDevice": True,
        "newCountry": False,
        "merchantRiskScore": 50,
        "customerAverageAmountRatio": 4.2,
        "sourceOldBalance": 10000.00,
        "sourceNewBalance": 7499.25,
        "destinationOldBalance": 500.00,
        "destinationNewBalance": 3000.75,
        "flaggedFraud": False,
    }
