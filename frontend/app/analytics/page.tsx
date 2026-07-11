"use client";

import { ShieldAlert, FolderSearch } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { MetricCard } from "@/components/dashboard/metric-card";
import { AnalyticsBarChart } from "@/components/analytics/analytics-bar-chart";
import { useAnalytics } from "@/hooks/use-analytics";

export default function AnalyticsPage() {
  const { data: summary, isLoading, error, usingMockData, refetch } = useAnalytics();

  return (
    <div className="space-y-6">
      <PageHeader title="Analytics" description="Fraud trend reporting across merchants, geography, timing, and case outcomes" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading analytics..." />
      ) : error || !summary ? (
        <ErrorState message={error ?? "Failed to load analytics"} onRetry={refetch} />
      ) : (
        <div className="space-y-6">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <MetricCard label="Total Alerts" value={String(summary.totalAlerts)} icon={ShieldAlert} tone="destructive" />
            <MetricCard label="Total Cases" value={String(summary.totalCases)} icon={FolderSearch} tone="info" />
          </div>

          <div className="grid gap-6 lg:grid-cols-2">
            <AnalyticsBarChart title="Alerts by Merchant Category" data={summary.alertsByMerchantCategory} color="#ef4444" />
            <AnalyticsBarChart title="Alerts by Country" data={summary.alertsByCountry} color="#f59e0b" />
          </div>

          <AnalyticsBarChart title="Alerts by Hour of Day" data={summary.alertsByHourOfDay} color="#3b82f6" />

          <div className="grid gap-6 lg:grid-cols-2">
            <AnalyticsBarChart title="Cases by Status" data={summary.casesByStatus} color="#8b5cf6" />
            <AnalyticsBarChart title="Cases by Decision" data={summary.casesByDecision} color="#10b981" />
          </div>
        </div>
      )}
    </div>
  );
}
