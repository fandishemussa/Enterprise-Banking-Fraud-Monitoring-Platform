"use client";

import { PageHeader } from "@/components/layout/page-header";
import { AlertsTable } from "@/components/tables/alerts-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useAlerts } from "@/hooks/use-alerts";
import { useAuth } from "@/hooks/use-auth";

export default function AlertsPage() {
  const { data: alerts, isLoading, error, usingMockData, refetch, changeStatus, assign, escalate } = useAlerts();
  const { hasRole } = useAuth();
  const seesOnlyOwnQueue = !hasRole("ADMIN", "VIEWER");

  return (
    <div className="space-y-6">
      <PageHeader
        title="Fraud Alerts"
        description={
          seesOnlyOwnQueue
            ? "Alerts assigned to you - generated from rule-based and ML fraud scoring"
            : "Alerts generated from rule-based and ML fraud scoring"
        }
      />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading alerts..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <AlertsTable alerts={alerts ?? []} onChangeStatus={changeStatus} onAssign={assign} onEscalate={escalate} />
      )}
    </div>
  );
}
