"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { CheckCircle2, ClipboardList, Gauge, XCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { DataTable } from "@/components/tables/data-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { MetricCard } from "@/components/dashboard/metric-card";
import { Badge } from "@/components/ui/badge";
import { useDataQualityResults, useDataQualityRuns } from "@/hooks/use-pipelines";
import { formatDate, formatPercent } from "@/lib/formatters";
import type { DataQualityResult } from "@/types";

export default function DataQualityPage() {
  const { data: runs, isLoading: runsLoading } = useDataQualityRuns();
  const { data: results, isLoading: resultsLoading, error, usingMockData, refetch } = useDataQualityResults();

  const totalChecks = results?.length ?? 0;
  const failedChecks = results?.filter((r) => !r.passed).length ?? 0;
  const warningChecks = results?.filter((r) => r.passed && r.recordsFailed > 0).length ?? 0;
  const qualityScore = totalChecks > 0 ? ((totalChecks - failedChecks) / totalChecks) * 100 : 100;

  const columns = useMemo<ColumnDef<DataQualityResult>[]>(
    () => [
      { accessorKey: "runId", header: "Run ID" },
      { accessorKey: "checkName", header: "Check" },
      { accessorKey: "recordsChecked", header: "Checked" },
      { accessorKey: "recordsPassed", header: "Passed" },
      { accessorKey: "recordsFailed", header: "Failed" },
      {
        accessorKey: "passed",
        header: "Result",
        cell: ({ getValue }) => (
          <Badge variant={getValue() ? "success" : "destructive"}>{getValue() ? "Passed" : "Failed"}</Badge>
        ),
      },
    ],
    [],
  );

  return (
    <div className="space-y-6">
      <PageHeader title="Data Quality" description="Automated data quality checks over clean_transactions" />
      {usingMockData && <DemoDataBanner />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard label="Quality Score" value={formatPercent(qualityScore)} icon={Gauge} tone="success" />
        <MetricCard label="Total Checks" value={String(totalChecks)} icon={ClipboardList} tone="info" />
        <MetricCard label="Failed Checks" value={String(failedChecks)} icon={XCircle} tone="destructive" />
        <MetricCard label="Warning Checks" value={String(warningChecks)} icon={CheckCircle2} tone="warning" />
      </div>

      <SectionCard title="Data Quality Runs" description={`${runs?.length ?? 0} recent audit run(s)`}>
        {runsLoading ? (
          <LoadingState label="Loading data quality runs..." />
        ) : (
          <ul className="divide-y divide-border">
            {(runs ?? []).map((run) => (
              <li key={run.runId} className="flex items-center justify-between py-2 text-sm">
                <span className="font-medium text-foreground">{run.runId}</span>
                <span className="text-muted-foreground">
                  {run.totalIssuesFound} issue(s) across {run.totalRecordsChecked} records
                </span>
                <span className="text-xs text-muted-foreground">{formatDate(run.startedAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </SectionCard>

      <SectionCard title="Data Quality Check Results">
        {resultsLoading ? (
          <LoadingState label="Loading check results..." />
        ) : error ? (
          <ErrorState message={error} onRetry={refetch} />
        ) : (
          <DataTable columns={columns} data={results ?? []} searchPlaceholder="Search checks..." emptyMessage="No data quality results found." />
        )}
      </SectionCard>
    </div>
  );
}
