"use client";

import { PageHeader } from "@/components/layout/page-header";
import { AccountsTable } from "@/components/tables/accounts-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useAccounts } from "@/hooks/use-customers";

export default function AccountsPage() {
  const { data: accounts, isLoading, error, usingMockData, refetch } = useAccounts();

  return (
    <div className="space-y-6">
      <PageHeader title="Accounts" description="Bank accounts held by customers" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading accounts..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <AccountsTable accounts={accounts ?? []} />
      )}
    </div>
  );
}
