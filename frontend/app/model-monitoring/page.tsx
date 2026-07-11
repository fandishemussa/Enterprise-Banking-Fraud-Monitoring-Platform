"use client";

import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { ModelMonitoringSummaryCards } from "@/components/model-monitoring/model-monitoring-summary-cards";
import { ModelScoreDistributionChart } from "@/components/model-monitoring/model-score-distribution-chart";
import { ModelFallbackRateChart } from "@/components/model-monitoring/model-fallback-rate-chart";
import { FeatureDriftTable } from "@/components/model-monitoring/feature-drift-table";
import { RecentPredictionsTable } from "@/components/model-monitoring/recent-predictions-table";
import { RetrainingHistoryTable } from "@/components/model-monitoring/retraining-history-table";
import { useModelMonitoring } from "@/hooks/use-model-monitoring";

export default function ModelMonitoringPage() {
  const { summary, predictions, scoreDistribution, drift, retrainingHistory, isLoading, error, usingMockData, refetch } =
    useModelMonitoring();

  return (
    <div className="space-y-6">
      <PageHeader title="Model Monitoring" description="ML scoring health, drift detection, and retraining history" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading model monitoring data..." />
      ) : error || !summary ? (
        <ErrorState message={error ?? "Failed to load model monitoring summary"} onRetry={refetch} />
      ) : (
        <div className="space-y-6">
          <ModelMonitoringSummaryCards summary={summary} />
          <div className="grid gap-6 lg:grid-cols-2">
            {scoreDistribution && <ModelScoreDistributionChart distribution={scoreDistribution} />}
            <ModelFallbackRateChart summary={summary} />
          </div>
          <FeatureDriftTable results={drift} />
          <RecentPredictionsTable predictions={predictions} />
          <RetrainingHistoryTable entries={retrainingHistory} />
        </div>
      )}
    </div>
  );
}
