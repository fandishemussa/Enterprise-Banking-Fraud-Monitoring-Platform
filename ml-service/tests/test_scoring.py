def test_score_single_transaction_fallback(client, sample_transaction):
    response = client.post("/api/v1/score", json=sample_transaction)

    assert response.status_code == 200
    body = response.json()
    assert body["transactionId"] == "TXN-1001"
    assert 0 <= body["mlScore"] <= 100
    assert body["riskLevel"] in {"LOW", "MEDIUM", "HIGH", "CRITICAL"}
    assert isinstance(body["explanation"], list)
    assert body["isAnomaly"] == (body["mlScore"] >= 61)
    # No trained model is present in this test environment, so fallback scoring is used.
    assert body["fallbackMode"] is True


def test_score_low_risk_transaction_is_low(client):
    payload = {
        "transactionId": "TXN-LOW",
        "amount": 10.0,
        "transactionType": "PAYMENT",
        "hourOfDay": 14,
    }

    response = client.post("/api/v1/score", json=payload)

    body = response.json()
    assert body["riskLevel"] == "LOW"
    assert body["isAnomaly"] is False


def test_score_rejects_invalid_hour_of_day(client, sample_transaction):
    sample_transaction["hourOfDay"] = 25

    response = client.post("/api/v1/score", json=sample_transaction)

    assert response.status_code == 422


def test_score_rejects_negative_amount(client, sample_transaction):
    sample_transaction["amount"] = -1

    response = client.post("/api/v1/score", json=sample_transaction)

    assert response.status_code == 422


def test_batch_score(client, sample_transaction):
    second = dict(sample_transaction, transactionId="TXN-1002", amount=50.0)

    response = client.post("/api/v1/batch-score", json={"transactions": [sample_transaction, second]})

    assert response.status_code == 200
    body = response.json()
    assert body["total"] == 2
    assert len(body["results"]) == 2
    assert {r["transactionId"] for r in body["results"]} == {"TXN-1001", "TXN-1002"}


def test_model_info_reports_not_loaded_when_no_model(client):
    response = client.get("/api/v1/model-info")

    assert response.status_code == 200
    body = response.json()
    if not body["modelLoaded"]:
        assert body["fallbackMode"] is True


def test_retrain_reports_missing_dataset(client, monkeypatch):
    monkeypatch.setattr(
        "app.main.dataset_available",
        lambda path: False,
    )

    response = client.post("/api/v1/retrain")

    assert response.status_code == 200
    body = response.json()
    assert body["success"] is False
    assert body["modelTrained"] is False
    assert "not found" in body["message"].lower()
