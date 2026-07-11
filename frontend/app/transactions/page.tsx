"use client";

import { useSearchParams } from "next/navigation";
import { PageHeader } from "@/components/layout/page-header";
import { TransactionsTable } from "@/components/tables/transactions-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useTransactions } from "@/hooks/use-transactions";

export default function TransactionsPage() {
  const { data: transactions, isLoading, error, usingMockData, refetch } = useTransactions();
  const searchParams = useSearchParams();
  const initialSearch = searchParams.get("search") ?? undefined;

  return (
    <div className="space-y-6">
      <PageHeader title="Transactions" description="All banking transactions ingested into the platform" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading transactions..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <TransactionsTable transactions={transactions ?? []} initialSearch={initialSearch} />
      )}
    </div>
  );
}
