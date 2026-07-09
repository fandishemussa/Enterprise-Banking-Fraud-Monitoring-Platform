import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.model import ModelManager, train_pipeline
from app.paysim_loader import dataset_available
from app.schemas import (
    BatchScoreRequest,
    BatchScoreResponse,
    HealthResponse,
    ModelInfoResponse,
    RetrainResponse,
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


@app.post("/api/v1/score", response_model=TransactionScoreResponse)
def score(request: TransactionScoreRequest) -> TransactionScoreResponse:
    return score_transaction(request, model_manager, settings.ml_service_version)


@app.post("/api/v1/batch-score", response_model=BatchScoreResponse)
def batch_score(request: BatchScoreRequest) -> BatchScoreResponse:
    results = [
        score_transaction(transaction, model_manager, settings.ml_service_version)
        for transaction in request.transactions
    ]
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


@app.post("/api/v1/retrain", response_model=RetrainResponse)
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

    return RetrainResponse(
        success=True,
        message="Model trained successfully.",
        modelTrained=True,
        modelVersion=metadata["modelVersion"],
        modelPath=metadata["modelPath"],
    )
