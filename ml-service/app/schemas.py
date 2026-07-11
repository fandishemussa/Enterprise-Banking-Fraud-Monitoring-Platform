from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel, Field


class TransactionScoreRequest(BaseModel):
    transactionId: str
    amount: float = Field(ge=0)
    transactionType: str
    hourOfDay: int = Field(ge=0, le=23)
    transactionFrequency: int = Field(default=0, ge=0)
    failedAttemptCount: int = Field(default=0, ge=0)
    countryRiskScore: int = Field(default=0, ge=0, le=100)
    newDevice: bool = False
    newCountry: bool = False
    merchantRiskScore: int = Field(default=0, ge=0, le=100)
    customerAverageAmountRatio: float = 1.0
    sourceOldBalance: float = 0.0
    sourceNewBalance: float = 0.0
    destinationOldBalance: float = 0.0
    destinationNewBalance: float = 0.0
    flaggedFraud: bool = False


class TransactionScoreResponse(BaseModel):
    transactionId: str
    mlScore: int
    isAnomaly: bool
    riskLevel: str
    explanation: List[str]
    modelVersion: str
    fallbackMode: bool


class BatchScoreRequest(BaseModel):
    transactions: List[TransactionScoreRequest]


class BatchScoreResponse(BaseModel):
    total: int
    results: List[TransactionScoreResponse]


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    modelLoaded: bool
    datasetAvailable: bool
    fallbackMode: bool


class ModelInfoResponse(BaseModel):
    modelName: Optional[str] = None
    modelType: Optional[str] = None
    modelVersion: Optional[str] = None
    trainingDate: Optional[datetime] = None
    datasetName: Optional[str] = None
    numberOfTrainingRows: Optional[int] = None
    featureList: List[str] = []
    modelPath: Optional[str] = None
    modelLoaded: bool
    fallbackMode: bool


class RetrainResponse(BaseModel):
    success: bool
    message: str
    modelTrained: bool
    modelVersion: Optional[str] = None
    modelPath: Optional[str] = None


class MlPredictionLog(BaseModel):
    predictionId: str
    transactionId: str
    mlScore: int
    riskLevel: str
    isAnomaly: bool
    fallbackMode: bool
    modelVersion: str
    featureValues: dict = {}
    createdAt: datetime


class ScoreDistribution(BaseModel):
    low: int
    medium: int
    high: int
    critical: int


class MonitoringSummaryResponse(BaseModel):
    totalPredictions: int
    averageMlScore: float
    highRiskPredictionCount: int
    criticalRiskPredictionCount: int
    anomalyCount: int
    fallbackModeCount: int
    fallbackRate: float
    modelVersion: Optional[str] = None
    lastTrainingDate: Optional[datetime] = None
    datasetAvailable: bool
    modelLoaded: bool


class FeatureDriftResult(BaseModel):
    featureName: str
    baselineAverage: float
    currentAverage: float
    driftScore: float
    driftStatus: str


class RetrainingHistoryEntry(BaseModel):
    modelVersion: str
    trainingDate: datetime
    datasetName: str
    numberOfTrainingRows: int
    status: str
    metrics: dict = {}
