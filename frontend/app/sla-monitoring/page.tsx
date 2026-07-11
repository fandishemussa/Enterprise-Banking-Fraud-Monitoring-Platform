"use client";

import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { SlaSummaryCards } from "@/components/sla/sla-summary-cards";
import { SlaBreachChart } from "@/components/sla/sla-breach-chart";
import { SlaAlertsTable } from "@/components/sla/sla-alerts-table";
import { SlaPolicyTable } from "@/components/sla/sla-policy-table";
import { useSlaMonitoring } from "@/hooks/use-sla-monitoring";
import { useAuth } from "@/hooks/use-auth";

export default function SlaMonitoringPage() {
  const { hasRole } = useAuth();
  const canManagePolicies = hasRole("ADMIN");
  const { summary, results, policies, isLoading, error, usingMockData, refetch, updatePolicy } = useSlaMonitoring();

  return (
    <div className="space-y-6">
      <PageHeader title="SLA Monitoring" description="Alert response and resolution SLA compliance, breaches, and policies" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading SLA data..." />
      ) : error || !summary ? (
        <ErrorState message={error ?? "Failed to load SLA summary"} onRetry={refetch} />
      ) : (
        <div className="space-y-6">
          <SlaSummaryCards summary={summary} />
          <SlaBreachChart summary={summary} />
          <SlaAlertsTable results={results} />
          <SlaPolicyTable policies={policies} canManage={canManagePolicies} onUpdate={updatePolicy} />
        </div>
      )}
    </div>
  );
}
