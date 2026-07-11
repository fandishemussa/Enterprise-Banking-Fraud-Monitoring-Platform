import { Activity, ShieldAlert, GitBranch, Percent } from "lucide-react";
import { MetricCard } from "@/components/dashboard/metric-card";
import { formatDate, formatNumber, formatPercent } from "@/lib/formatters";
import type { ModelMonitoringSummary } from "@/types";

export function ModelMonitoringSummaryCards({ summary }: { summary: ModelMonitoringSummary }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard
        label="Total Predictions"
        value={formatNumber(summary.totalPredictions)}
        hint={`Avg score ${summary.averageMlScore.toFixed(1)}`}
        icon={Activity}
        tone="info"
      />
      <MetricCard
        label="High/Critical Predictions"
        value={String(summary.highRiskPredictionCount + summary.criticalRiskPredictionCount)}
        hint={`${summary.anomalyCount} flagged as anomalies`}
        icon={ShieldAlert}
        tone={summary.criticalRiskPredictionCount > 0 ? "destructive" : "warning"}
      />
      <MetricCard
        label="Fallback Rate"
        value={formatPercent(summary.fallbackRate)}
        hint={`${summary.fallbackModeCount} fallback-scored`}
        icon={Percent}
        tone={summary.fallbackRate > 20 ? "warning" : "neutral"}
      />
      <MetricCard
        label="Model Version"
        value={summary.modelVersion ?? "Fallback only"}
        hint={summary.lastTrainingDate ? `Trained ${formatDate(summary.lastTrainingDate)}` : "Never trained"}
        icon={GitBranch}
        tone={summary.modelLoaded ? "success" : "neutral"}
      />
    </div>
  );
}
