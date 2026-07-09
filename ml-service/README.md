# ML Fraud Scoring Service

A standalone FastAPI microservice that scores banking transactions for fraud risk using an
Isolation Forest model trained on the PaySim dataset, with a rule-based fallback so it always
answers even without a trained model or dataset present.

## Overview

This service is called by the Spring Boot backend (`risk` module) to get an ML-based risk score
that gets combined with the backend's own rule-based score. It never talks to the backend's
database directly — it's a pure scoring function: transaction in, risk score out.

## Architecture

```
Spring Boot backend
        |
        | POST /api/v1/score
        v
FastAPI ml-service
        |
        |-- model loaded?  --yes--> Isolation Forest --> normalize decision_function --> mlScore
        |         |
        |         no
        |         v
        |-- fallback rule engine (scoring.py) --> mlScore
        |
        v
TransactionScoreResponse (mlScore, isAnomaly, riskLevel, explanation, fallbackMode)
```

Training data flow (`POST /api/v1/retrain` or `scripts/train_model.py`):

```
PaySim CSV (data/raw/paysim/)
        |
        v
paysim_loader.load_paysim_raw       <- reads the raw file, validates required columns
        |
        v
preprocessing.map_paysim_to_platform_schema   <- renames PaySim columns, synthesizes the
        |                                          enterprise-only fields (device, country,
        |                                          merchant risk, frequency, etc.)
        v
feature_engineering.build_feature_matrix      <- fixed 16-column feature order
        |
        v
IsolationForest.fit  ->  models/fraud_model.joblib + models/model_metadata.json
```

## Folder structure

```
ml-service/
├── app/
│   ├── main.py                 # FastAPI app + routes
│   ├── schemas.py               # Pydantic request/response models
│   ├── config.py                # Settings (env-driven), path resolution
│   ├── scoring.py                # Fallback rule engine + ML/fallback dispatcher
│   ├── feature_engineering.py    # Fixed feature order, transaction-type encoding
│   ├── model.py                  # ModelManager (load/save/train/predict) + train_pipeline
│   ├── paysim_loader.py           # Locates and reads the raw PaySim CSV
│   ├── preprocessing.py           # PaySim -> platform schema mapping
│   └── utils.py                   # risk_level_from_score, timestamps
├── scripts/
│   ├── train_model.py             # CLI: train from PaySim, save artifact
│   ├── prepare_paysim_data.py      # CLI: map raw PaySim -> processed CSV (debugging aid)
│   └── generate_sample_data.py     # CLI: generate a small PaySim-shaped CSV for testing
├── models/                          # fraud_model.joblib + model_metadata.json (gitignored)
├── tests/
├── requirements.txt
├── Dockerfile
└── .dockerignore
```

## API endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/health` | Service health, model/dataset availability |
| POST | `/api/v1/score` | Score a single transaction |
| POST | `/api/v1/batch-score` | Score multiple transactions |
| GET | `/api/v1/model-info` | Currently loaded model's metadata |
| POST | `/api/v1/retrain` | Train (or retrain) the model from the PaySim dataset |

## How fallback scoring works

If no trained model is present at `models/fraud_model.joblib` (or a prediction throws), the
service falls back to a fixed point-rule table (`app/scoring.py`), capped at 100:

| Rule | Points |
|---|---|
| amount > 10,000 | +40 |
| amount > 5,000 (and not > 10,000) | +25 |
| hour of day between 0–5 | +15 |
| transaction frequency > 5 | +25 |
| failed attempt count > 2 | +20 |
| country risk score > 70 | +20 |
| new device | +20 |
| new country | +25 |
| merchant risk score > 70 | +20 |
| customer average amount ratio > 3 | +25 |

Risk levels: 0–30 LOW, 31–60 MEDIUM, 61–80 HIGH, 81–100 CRITICAL. `isAnomaly` is true when
`mlScore >= 61`. The response always includes `fallbackMode: true` in this path so callers know
no trained model was used.

## Running locally

```bash
cd ml-service
python -m venv .venv
.venv\Scripts\activate        # Windows
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

The service starts and answers `/health` and `/api/v1/score` immediately, in fallback mode,
with no dataset or model file required.

## Running tests

```bash
cd ml-service
pytest
```

Tests pass with no PaySim dataset and no trained model present.

## Training the model with PaySim

1. Get the [PaySim Synthetic Financial Datasets For Fraud Detection](https://www.kaggle.com/datasets/ealaxi/paysim1)
   dataset from Kaggle (or generate a small stand-in with `python scripts/generate_sample_data.py`).
2. Place the CSV file in `data/raw/paysim/` (relative to the project root, i.e. one level above
   `ml-service/`), or set `PAYSIM_DATA_PATH` to wherever the file already lives — it accepts
   either a directory (first `*.csv` inside it is used) or a direct path to the CSV file.
3. Train:
   ```bash
   python scripts/train_model.py
   ```
   or call `POST /api/v1/retrain` while the service is running.
4. Restart the service (or call `/api/v1/retrain` again) to pick up the new model — `/health`
   and `/api/v1/model-info` will then report `modelLoaded: true`.

### PaySim column mapping

| PaySim column | Platform field |
|---|---|
| `step` | `hour_of_day` (derived: `step % 24`) |
| `type` | `transactionType` |
| `amount` | `amount` |
| `nameOrig` | `sourceAccountId` |
| `oldbalanceOrg` | `sourceOldBalance` |
| `newbalanceOrig` | `sourceNewBalance` |
| `nameDest` | `destinationAccountId` |
| `oldbalanceDest` | `destinationOldBalance` |
| `newbalanceDest` | `destinationNewBalance` |
| `isFraud` | `fraudLabel` (kept for a future supervised model) |
| `isFlaggedFraud` | `flaggedFraud` |

`transactionId`, `customerId`, `deviceId`, `ipAddress`, `country`, `merchantCategory`,
`channel`, `status`, `createdAt`, and the risk-signal fields (`transactionFrequency`,
`countryRiskScore`, `newDevice`, `newCountry`, `merchantRiskScore`,
`customerAverageAmountRatio`) are synthesized in `app/preprocessing.py` since PaySim has no
equivalent columns — they're mildly correlated with `isFraud` so the demo model has a real
signal to find.

## Example request/response

`POST /api/v1/score`

```json
{
  "transactionId": "TXN-1001",
  "amount": 2500.75,
  "transactionType": "TRANSFER",
  "hourOfDay": 23,
  "transactionFrequency": 7,
  "failedAttemptCount": 2,
  "countryRiskScore": 60,
  "newDevice": true,
  "newCountry": false,
  "merchantRiskScore": 50,
  "customerAverageAmountRatio": 4.2,
  "sourceOldBalance": 10000.00,
  "sourceNewBalance": 7499.25,
  "destinationOldBalance": 500.00,
  "destinationNewBalance": 3000.75,
  "flaggedFraud": false
}
```

```json
{
  "transactionId": "TXN-1001",
  "mlScore": 78,
  "isAnomaly": true,
  "riskLevel": "HIGH",
  "explanation": [
    "Amount significantly exceeds customer's average",
    "New device detected"
  ],
  "modelVersion": "1.0.0",
  "fallbackMode": true
}
```

## Docker

```bash
docker build -t ml-fraud-scoring-service .
docker run -p 8000:8000 ml-fraud-scoring-service
```

## Spring Boot integration

The Spring Boot backend calls this service over HTTP:

```
POST http://localhost:8000/api/v1/score
```

or, when both services run in the same Docker network:

```
POST http://ml-service:8000/api/v1/score
```

All response fields are camelCase (`transactionId`, `mlScore`, `isAnomaly`, `riskLevel`,
`explanation`, `modelVersion`, `fallbackMode`) to map directly onto Spring Boot DTOs with no
translation layer needed.
