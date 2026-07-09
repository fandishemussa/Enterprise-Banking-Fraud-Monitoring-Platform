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
    fallback_mode_enabled: bool = True
    # PaySim's real Kaggle export is 6M+ rows; training on all of it can exceed available RAM
    # on a dev machine. 0 means "use every row" for users with enough memory.
    train_sample_rows: int = 200_000
    cors_allowed_origins: str = "http://localhost:3000"

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


settings = Settings()
