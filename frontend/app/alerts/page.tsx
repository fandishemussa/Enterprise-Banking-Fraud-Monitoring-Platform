"use client";

import { PageHeader } from "@/components/layout/page-header";
import { AlertsTable } from "@/components/tables/alerts-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useAlerts } from "@/hooks/use-alerts";

export default function AlertsPage() {
  const { data: alerts, isLoading, error, usingMockData, refetch, changeStatus, assign } = useAlerts();

  return (
    <div className="space-y-6">
      <PageHeader title="Fraud Alerts" description="Alerts generated from rule-based and ML fraud scoring" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading alerts..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <AlertsTable alerts={alerts ?? []} onChangeStatus={changeStatus} onAssign={assign} />
      )}
    </div>
  );
}
