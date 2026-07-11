"use client";

import { PageHeader } from "@/components/layout/page-header";
import { CasesTable } from "@/components/tables/cases-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { ExportCsvButton } from "@/components/shared/export-csv-button";
import { useCases } from "@/hooks/use-cases";
import { useAuth } from "@/hooks/use-auth";

export default function CasesPage() {
  const { data: cases, isLoading, error, usingMockData, refetch, bulkUpdate } = useCases();
  const { hasRole } = useAuth();
  const seesOnlyOwnQueue = !hasRole("ADMIN", "VIEWER");

  return (
    <div className="space-y-6">
      <PageHeader
        title="Investigation Cases"
        description={
          seesOnlyOwnQueue
            ? "Cases assigned to you - one case per alert"
            : "Fraud analyst investigation workflow, one case per alert"
        }
        action={<ExportCsvButton filename="investigation-cases" rows={cases ?? []} />}
      />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading cases..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <CasesTable cases={cases ?? []} onBulkUpdate={bulkUpdate} />
      )}
    </div>
  );
}
