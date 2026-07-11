"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { CsvUploadCard } from "@/components/shared/csv-upload-card";
import { DataTable } from "@/components/tables/data-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { MetricCard } from "@/components/dashboard/metric-card";
import { PipelineRecordsChart } from "@/components/dashboard/pipeline-records-chart";
import { PipelineStatusBadge } from "@/components/badges/pipeline-status-badge";
import { usePipelineMetrics, usePipelineRuns } from "@/hooks/use-pipelines";
import { formatDate, formatDurationMs, formatPercent, titleCase } from "@/lib/formatters";
import type { PipelineRun } from "@/types";
import { CheckCircle2, Clock, Workflow, XCircle } from "lucide-react";

export default function PipelineObservabilityPage() {
  const { data: metrics, isLoading: metricsLoading, refetch: refetchMetrics } = usePipelineMetrics();
  const { data: runs, isLoading: runsLoading, error, usingMockData, refetch } = usePipelineRuns();

  function handleUploaded() {
    refetch();
    refetchMetrics();
  }

  const chartData = useMemo(
    () => (runs ?? []).map((run) => ({ name: run.runId, processed: run.recordsProcessed, failed: run.recordsFailed })).reverse(),
    [runs],
  );

  const columns = useMemo<ColumnDef<PipelineRun>[]>(
    () => [
      { accessorKey: "runId", header: "Run ID" },
      { accessorKey: "pipelineType", header: "Type", cell: ({ getValue }) => titleCase(getValue() as string) },
      { accessorKey: "status", header: "Status", cell: ({ getValue }) => <PipelineStatusBadge status={getValue() as never} /> },
      { accessorKey: "recordsProcessed", header: "Processed" },
      { accessorKey: "recordsAccepted", header: "Accepted" },
      { accessorKey: "recordsRejected", header: "Rejected" },
      { accessorKey: "recordsFailed", header: "Failed" },
      { accessorKey: "durationMs", header: "Duration", cell: ({ getValue }) => formatDurationMs(getValue() as number) },
      { accessorKey: "startedAt", header: "Started", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return (
    <div className="space-y-6">
      <PageHeader title="Pipeline Observability" description="Ingestion and data quality pipeline run history" />
      {usingMockData && <DemoDataBanner />}

      <CsvUploadCard onUploaded={handleUploaded} />

      {metricsLoading || !metrics ? (
        <LoadingState label="Loading pipeline metrics..." />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <MetricCard label="Total Runs" value={String(metrics.totalRuns)} icon={Workflow} tone="info" />
          <MetricCard label="Success Rate" value={formatPercent(metrics.successRatePercent)} icon={CheckCircle2} tone="success" />
          <MetricCard label="Failed Runs" value={String(metrics.failedRuns)} icon={XCircle} tone="destructive" />
          <MetricCard label="Avg Duration" value={formatDurationMs(metrics.averageDurationMs)} icon={Clock} tone="neutral" />
        </div>
      )}

      <SectionCard title="Records Processed vs. Failed" description="Per pipeline run">
        <PipelineRecordsChart data={chartData} />
      </SectionCard>

      <SectionCard title="Pipeline Runs">
        {runsLoading ? (
          <LoadingState label="Loading pipeline runs..." />
        ) : error ? (
          <ErrorState message={error} onRetry={refetch} />
        ) : (
          <DataTable columns={columns} data={runs ?? []} searchPlaceholder="Search pipeline runs..." emptyMessage="No pipeline runs found." />
        )}
      </SectionCard>
    </div>
  );
}
