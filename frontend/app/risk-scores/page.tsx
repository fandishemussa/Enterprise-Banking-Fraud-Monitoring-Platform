"use client";

import { PageHeader } from "@/components/layout/page-header";
import { RiskScoresTable } from "@/components/tables/risk-scores-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useRiskScores } from "@/hooks/use-transactions";

export default function RiskScoresPage() {
  const { data: riskScores, isLoading, error, usingMockData, refetch } = useRiskScores();

  return (
    <div className="space-y-6">
      <PageHeader title="Risk Scores" description="Rule-based and ML risk scores for every scored transaction" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading risk scores..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <RiskScoresTable riskScores={riskScores ?? []} />
      )}
    </div>
  );
}
