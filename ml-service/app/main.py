import hmac
import logging
from contextlib import asynccontextmanager
from typing import List

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.drift_detection import compute_drift
from app.model import ModelManager, train_pipeline
from app.monitoring import append_retraining_history, get_retraining_history, get_score_distribution, get_summary
from app.paysim_loader import dataset_available
from app.prediction_logger import log_prediction, read_predictions
from app.schemas import (
    BatchScoreRequest,
    BatchScoreResponse,
    FeatureDriftResult,
    HealthResponse,
    ModelInfoResponse,
    MlPredictionLog,
    MonitoringSummaryResponse,
    RetrainResponse,
    RetrainingHistoryEntry,
    ScoreDistribution,
    TransactionScoreRequest,
    TransactionScoreResponse,
)
from app.scoring import score_transaction

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

model_manager = ModelManager(settings.model_path_resolved, settings.model_metadata_path_resolved)


@asynccontextmanager
async def lifespan(_: FastAPI):
    loaded = model_manager.load()
    if loaded:
        logger.info("Loaded model from %s", settings.model_path_resolved)
    else:
        logger.info("No trained model found at %s - serving fallback scores until /api/v1/retrain "
                     "is called", settings.model_path_resolved)
    yield


app = FastAPI(
    title=settings.ml_service_name,
    version=settings.ml_service_version,
    description="ML fraud scoring service for the Enterprise Banking Fraud Monitoring Platform",
    lifespan=lifespan,
)

# The Next.js dashboard (a different origin) calls this service directly from the browser;
# server-to-server calls (e.g. from the Spring Boot backend) are unaffected by CORS.
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_allowed_origins_list,
    allow_methods=["GET", "POST", "PATCH", "OPTIONS"],
    allow_headers=["*"],
)


def require_api_key(x_api_key: str | None = Header(default=None)) -> None:
    """Guards the compute-costly / abusable endpoints (score, batch-score, retrain). No-op if
    ML_API_KEY isn't configured, so local dev / the existing test suite keep working unchanged."""
    if not settings.ml_api_key:
        return
    if not x_api_key or not hmac.compare_digest(x_api_key, settings.ml_api_key):
        raise HTTPException(status_code=401, detail="Missing or invalid X-API-Key")


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="healthy",
        service=settings.ml_service_name,
        version=settings.ml_service_version,
        modelLoaded=model_manager.is_loaded(),
        datasetAvailable=dataset_available(settings.paysim_data_path_resolved),
        fallbackMode=not model_manager.is_loaded(),
    )


@app.post("/api/v1/score", response_model=TransactionScoreResponse, dependencies=[Depends(require_api_key)])
def score(request: TransactionScoreRequest) -> TransactionScoreResponse:
    response = score_transaction(request, model_manager, settings.ml_service_version)
    log_prediction(settings.predictions_log_path_resolved, request, response)
    return response


@app.post("/api/v1/batch-score", response_model=BatchScoreResponse, dependencies=[Depends(require_api_key)])
def batch_score(request: BatchScoreRequest) -> BatchScoreResponse:
    results = []
    for transaction in request.transactions:
        result = score_transaction(transaction, model_manager, settings.ml_service_version)
        log_prediction(settings.predictions_log_path_resolved, transaction, result)
        results.append(result)
    return BatchScoreResponse(total=len(results), results=results)


@app.get("/api/v1/model-info", response_model=ModelInfoResponse)
def model_info() -> ModelInfoResponse:
    info = model_manager.get_info()
    return ModelInfoResponse(
        modelName=info.get("modelName"),
        modelType=info.get("modelType"),
        modelVersion=info.get("modelVersion"),
        trainingDate=info.get("trainingDate"),
        datasetName=info.get("datasetName"),
        numberOfTrainingRows=info.get("numberOfTrainingRows"),
        featureList=info.get("featureList", []),
        modelPath=info.get("modelPath", str(settings.model_path_resolved)),
        modelLoaded=model_manager.is_loaded(),
        fallbackMode=not model_manager.is_loaded(),
    )


@app.post("/api/v1/retrain", response_model=RetrainResponse, dependencies=[Depends(require_api_key)])
def retrain() -> RetrainResponse:
    if not dataset_available(settings.paysim_data_path_resolved):
        return RetrainResponse(
            success=False,
            message="PaySim dataset not found. Place the dataset in data/raw/paysim/ before training.",
            modelTrained=False,
        )

    try:
        metadata = train_pipeline(
            settings.paysim_data_path_resolved,
            model_manager,
            settings.ml_service_version,
            settings.model_path_resolved,
            max_rows=settings.train_sample_rows,
        )
    except Exception as ex:
        logger.exception("Model training failed")
        raise HTTPException(status_code=500, detail=f"Model training failed: {ex}") from ex

    append_retraining_history(settings.retraining_history_log_path_resolved, metadata)

    return RetrainResponse(
        success=True,
        message="Model trained successfully.",
        modelTrained=True,
        modelVersion=metadata["modelVersion"],
        modelPath=metadata["modelPath"],
    )


@app.get("/api/v1/monitoring/summary", response_model=MonitoringSummaryResponse)
def monitoring_summary() -> MonitoringSummaryResponse:
    return get_summary(
        settings.predictions_log_path_resolved,
        model_manager,
        dataset_available(settings.paysim_data_path_resolved),
    )


@app.get("/api/v1/monitoring/predictions", response_model=List[MlPredictionLog])
def monitoring_predictions(limit: int = 100) -> List[MlPredictionLog]:
    return read_predictions(settings.predictions_log_path_resolved, limit=limit)


@app.get("/api/v1/monitoring/score-distribution", response_model=ScoreDistribution)
def monitoring_score_distribution() -> ScoreDistribution:
    return get_score_distribution(settings.predictions_log_path_resolved)


@app.get("/api/v1/monitoring/drift", response_model=List[FeatureDriftResult])
def monitoring_drift() -> List[FeatureDriftResult]:
    recent_predictions = read_predictions(settings.predictions_log_path_resolved, limit=500)
    return compute_drift(model_manager, recent_predictions)


@app.get("/api/v1/monitoring/retraining-history", response_model=List[RetrainingHistoryEntry])
def monitoring_retraining_history() -> List[RetrainingHistoryEntry]:
    return get_retraining_history(settings.retraining_history_log_path_resolved)
