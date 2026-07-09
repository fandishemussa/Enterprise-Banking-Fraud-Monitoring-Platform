"use client";

import { PageHeader } from "@/components/layout/page-header";
import { CustomersTable } from "@/components/tables/customers-table";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { useCustomers } from "@/hooks/use-customers";

export default function CustomersPage() {
  const { data: customers, isLoading, error, usingMockData, refetch } = useCustomers();

  return (
    <div className="space-y-6">
      <PageHeader title="Customers" description="Bank customers and their risk profiles" />
      {usingMockData && <DemoDataBanner />}
      {isLoading ? (
        <LoadingState label="Loading customers..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <CustomersTable customers={customers ?? []} />
      )}
    </div>
  );
}
