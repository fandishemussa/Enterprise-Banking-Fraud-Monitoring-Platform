"use client";

import { useCallback, useEffect, useState } from "react";
import {
  getModelDrift,
  getModelMonitoringSummary,
  getModelPredictions,
  getModelScoreDistribution,
  getRetrainingHistory,
} from "@/lib/api";
import type {
  FeatureDriftResult,
  MlPredictionLog,
  ModelMonitoringSummary,
  RetrainingHistoryEntry,
  ScoreDistribution,
} from "@/types";

export function useModelMonitoring() {
  const [summary, setSummary] = useState<ModelMonitoringSummary | null>(null);
  const [predictions, setPredictions] = useState<MlPredictionLog[]>([]);
  const [scoreDistribution, setScoreDistribution] = useState<ScoreDistribution | null>(null);
  const [drift, setDrift] = useState<FeatureDriftResult[]>([]);
  const [retrainingHistory, setRetrainingHistory] = useState<RetrainingHistoryEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingMockData, setUsingMockData] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    Promise.all([
      getModelMonitoringSummary(),
      getModelPredictions(),
      getModelScoreDistribution(),
      getModelDrift(),
      getRetrainingHistory(),
    ])
      .then(([summaryResult, predictionsResult, distributionResult, driftResult, historyResult]) => {
        setSummary(summaryResult.data);
        setPredictions(predictionsResult.data);
        setScoreDistribution(distributionResult.data);
        setDrift(driftResult.data);
        setRetrainingHistory(historyResult.data);
        setUsingMockData(
          summaryResult.usingMockData ||
            predictionsResult.usingMockData ||
            distributionResult.usingMockData ||
            driftResult.usingMockData ||
            historyResult.usingMockData,
        );
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load model monitoring data"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => load(), [load]);

  return {
    summary,
    predictions,
    scoreDistribution,
    drift,
    retrainingHistory,
    isLoading,
    error,
    usingMockData,
    refetch: load,
  };
}
