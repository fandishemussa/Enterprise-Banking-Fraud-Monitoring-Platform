from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

# ml-service/ root, regardless of the process's current working directory.
BASE_DIR = Path(__file__).resolve().parent.parent


def _resolve(path_str: str) -> Path:
    path = Path(path_str)
    return path if path.is_absolute() else (BASE_DIR / path).resolve()


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False)

    ml_service_name: str = "ml-fraud-scoring-service"
    ml_service_version: str = "1.0.0"
    model_path: str = "models/fraud_model.joblib"
    model_metadata_path: str = "models/model_metadata.json"
    paysim_data_path: str = "../data/raw/paysim/"
    # Model monitoring's append-only logs (see app/monitoring.py, app/prediction_logger.py) -
    # this service has no database, so JSONL files are the whole "storage layer" for this feature.
    predictions_log_path: str = "logs/predictions.jsonl"
    retraining_history_log_path: str = "logs/retraining_history.jsonl"
    fallback_mode_enabled: bool = True
    # PaySim's real Kaggle export is 6M+ rows; training on all of it can exceed available RAM
    # on a dev machine. 0 means "use every row" for users with enough memory.
    train_sample_rows: int = 200_000
    cors_allowed_origins: str = "http://localhost:3000"
    # Shared secret the Spring Boot backend must send as X-API-Key on /score, /batch-score, and
    # /retrain (the compute-costly / abusable endpoints). Left blank (the default), those endpoints
    # stay open - matching this project's "secure when configured, permissive for local dev"
    # convention (see SECURITY_ENABLED on the backend). /health and /model-info stay unauthenticated
    # regardless, since the dashboard calls them directly from the browser.
    ml_api_key: str = ""

    @property
    def cors_allowed_origins_list(self) -> list[str]:
        return [origin.strip() for origin in self.cors_allowed_origins.split(",") if origin.strip()]

    @property
    def model_path_resolved(self) -> Path:
        return _resolve(self.model_path)

    @property
    def model_metadata_path_resolved(self) -> Path:
        return _resolve(self.model_metadata_path)

    @property
    def paysim_data_path_resolved(self) -> Path:
        return _resolve(self.paysim_data_path)

    @property
    def predictions_log_path_resolved(self) -> Path:
        return _resolve(self.predictions_log_path)

    @property
    def retraining_history_log_path_resolved(self) -> Path:
        return _resolve(self.retraining_history_log_path)


settings = Settings()
