def test_health_returns_healthy_status(client):
    response = client.get("/health")

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "healthy"
    assert body["service"]
    assert "modelLoaded" in body
    assert "datasetAvailable" in body
    assert "fallbackMode" in body


def test_health_reports_fallback_when_no_model_present(client):
    response = client.get("/health")

    body = response.json()
    if not body["modelLoaded"]:
        assert body["fallbackMode"] is True
