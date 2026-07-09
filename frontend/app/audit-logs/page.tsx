"use client";

import { PageHeader } from "@/components/layout/page-header";
import { AuditLogsTable } from "@/components/tables/audit-logs-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useAuditLogs } from "@/hooks/use-pipelines";

export default function AuditLogsPage() {
  const { data: logs, isLoading, error, usingMockData, refetch } = useAuditLogs();

  return (
    <div className="space-y-6">
      <PageHeader title="Audit Logs" description="Immutable record of every state-changing action across the platform" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading audit logs..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <AuditLogsTable logs={logs ?? []} />
      )}
    </div>
  );
}
