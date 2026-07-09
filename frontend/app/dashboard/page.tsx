"use client";

import {
  Activity,
  AlertOctagon,
  CheckCircle2,
  Cpu,
  Gauge,
  ShieldAlert,
  ShieldCheck,
  Workflow,
} from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { MetricCard } from "@/components/dashboard/metric-card";
import { FraudTrendChart } from "@/components/dashboard/fraud-trend-chart";
import { RiskDistributionChart } from "@/components/dashboard/risk-distribution-chart";
import { AlertStatusChart } from "@/components/dashboard/alert-status-chart";
import { PipelineRecordsChart } from "@/components/dashboard/pipeline-records-chart";
import { RecentAlerts } from "@/components/dashboard/recent-alerts";
import { PipelineHealthSummary } from "@/components/dashboard/pipeline-health-summary";
import { SectionCard } from "@/components/shared/section-card";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useDashboard } from "@/hooks/use-dashboard";
import { usePipelineMetrics } from "@/hooks/use-pipelines";
import { useMlHealth } from "@/hooks/use-ml-service";
import { formatNumber, formatPercent } from "@/lib/formatters";

export default function DashboardPage() {
  const { data: summary, isLoading, error, usingMockData, refetch } = useDashboard();
  const { data: pipelineMetrics } = usePipelineMetrics();
  const { data: mlHealth } = useMlHealth();

  if (isLoading) return <LoadingState label="Loading dashboard..." />;
  if (error || !summary) return <ErrorState message={error ?? "Unable to load dashboard summary."} onRetry={refetch} />;

  return (
    <div className="space-y-6">
      <PageHeader title="Executive Dashboard" description="Real-time fraud monitoring overview across the platform" />
      {usingMockData && <DemoDataBanner />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard label="Total Transactions" value={formatNumber(summary.totalTransactions)} icon={Activity} tone="info" />
        <MetricCard label="High-Risk Transactions" value={formatNumber(summary.highRiskTransactions)} icon={Gauge} tone="warning" />
        <MetricCard label="Critical-Risk Transactions" value={formatNumber(summary.criticalRiskTransactions)} icon={AlertOctagon} tone="destructive" />
        <MetricCard label="Open Fraud Alerts" value={formatNumber(summary.openFraudAlerts)} icon={ShieldAlert} tone="destructive" />
        <MetricCard label="Confirmed Fraud Cases" value={formatNumber(summary.confirmedFraudCases)} icon={ShieldCheck} tone="destructive" />
        <MetricCard label="False Positive Rate" value={formatPercent(summary.falsePositiveRate)} icon={CheckCircle2} tone="success" />
        <MetricCard label="Average Risk Score" value={summary.averageRiskScore.toFixed(1)} icon={Gauge} tone="info" />
        <MetricCard label="Pipeline Success Rate" value={formatPercent(summary.pipelineSuccessRate)} icon={Workflow} tone="success" />
        <MetricCard label="Records Processed Today" value={formatNumber(summary.recordsProcessedToday)} icon={Activity} tone="neutral" />
        <MetricCard
          label="ML Service Status"
          value={summary.mlServiceOnline ? "Online" : "Offline"}
          icon={Cpu}
          tone={summary.mlServiceOnline ? "success" : "destructive"}
        />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <SectionCard title="Fraud Trend Over Time" description="Daily transaction volume vs. fraud alerts">
          <FraudTrendChart data={summary.fraudTrend} />
        </SectionCard>
        <SectionCard title="Risk Level Distribution" description="Scored transactions by risk tier">
          <RiskDistributionChart data={summary.riskDistribution} />
        </SectionCard>
        <SectionCard title="Alert Status Distribution" description="Fraud alerts grouped by current status">
          <AlertStatusChart data={summary.alertStatusDistribution} />
        </SectionCard>
        <SectionCard title="Pipeline Records Processed vs. Failed" description="Most recent ingestion and quality runs">
          <PipelineRecordsChart data={summary.pipelineRecordsChart} />
        </SectionCard>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <SectionCard title="Recent High-Risk Alerts" className="lg:col-span-2" contentClassName="pt-2">
          <RecentAlerts alerts={summary.recentAlerts} />
        </SectionCard>
        <SectionCard title="Pipeline Health Summary">
          {pipelineMetrics ? (
            <PipelineHealthSummary metrics={pipelineMetrics} />
          ) : (
            <LoadingState label="Loading pipeline metrics..." />
          )}
        </SectionCard>
      </div>

      <SectionCard title="ML Service Summary" description="Fraud scoring model health">
        {mlHealth ? (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <p className="text-xs text-muted-foreground">Status</p>
              <p className="text-sm font-medium text-foreground">{mlHealth.status}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Model Loaded</p>
              <p className="text-sm font-medium text-foreground">{mlHealth.modelLoaded ? "Yes" : "No"}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Fallback Mode</p>
              <p className="text-sm font-medium text-foreground">{mlHealth.fallbackMode ? "Active" : "Inactive"}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground">Dataset Available</p>
              <p className="text-sm font-medium text-foreground">{mlHealth.datasetAvailable ? "Yes" : "No"}</p>
            </div>
          </div>
        ) : (
          <LoadingState label="Checking ML service..." />
        )}
      </SectionCard>
    </div>
  );
}
