"use client";

import { PageHeader } from "@/components/layout/page-header";
import { DeadLetterTable } from "@/components/tables/dead-letter-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useDeadLetterTransactions } from "@/hooks/use-pipelines";

export default function DeadLetterPage() {
  const { data: records, isLoading, error, usingMockData, refetch, retry, ignore } = useDeadLetterTransactions();

  return (
    <div className="space-y-6">
      <PageHeader title="Dead Letter Queue" description="Rows that failed to parse structurally during ingestion" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading dead letter records..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <DeadLetterTable records={records ?? []} onRetry={retry} onIgnore={ignore} />
      )}
    </div>
  );
}
