"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { CustomerRiskProfileHeader } from "@/components/customer-profile/customer-risk-profile-header";
import { CustomerLockPanel } from "@/components/customer-profile/customer-lock-panel";
import { CustomerRiskKpiCards } from "@/components/customer-profile/customer-risk-kpi-cards";
import { CustomerBehaviorSummary } from "@/components/customer-profile/customer-behavior-summary";
import { CustomerRiskTrendChart } from "@/components/customer-profile/customer-risk-trend-chart";
import { CustomerTransactionVolumeChart } from "@/components/customer-profile/customer-transaction-volume-chart";
import { CustomerAlertTrendChart } from "@/components/customer-profile/customer-alert-trend-chart";
import { CustomerDevicesTable } from "@/components/customer-profile/customer-devices-table";
import { CustomerCountriesTable } from "@/components/customer-profile/customer-countries-table";
import { CustomerRecentTransactions } from "@/components/customer-profile/customer-recent-transactions";
import { CustomerAlertHistory } from "@/components/customer-profile/customer-alert-history";
import { CustomerCaseHistory } from "@/components/customer-profile/customer-case-history";
import { LinkedCustomersCard } from "@/components/customer-profile/linked-customers-card";
import { useCustomerRiskProfile } from "@/hooks/use-customer-risk-profile";

export default function CustomerRiskProfilePage({ params }: { params: Promise<{ customerId: string }> }) {
  const { customerId } = use(params);
  const { data: profile, isLoading, error, usingMockData, refetch } = useCustomerRiskProfile(customerId);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2">
        <Link href="/customers" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Back to Customers
        </Link>
      </div>
      <PageHeader title="Customer Risk 360" description="Full risk profile, behavior, and history for this customer" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading customer risk profile..." />
      ) : error || !profile ? (
        <ErrorState message={error ?? "Customer not found"} onRetry={refetch} />
      ) : (
        <div className="space-y-6">
          <CustomerRiskProfileHeader profile={profile} />
          <CustomerLockPanel customerId={customerId} status={profile.status} onStatusChange={refetch} />
          <CustomerRiskKpiCards profile={profile} />
          <div className="grid gap-6 lg:grid-cols-2">
            <CustomerRiskTrendChart data={profile.riskTrendData} />
            <CustomerTransactionVolumeChart data={profile.transactionVolumeTrend} />
          </div>
          <div className="grid gap-6 lg:grid-cols-2">
            <CustomerAlertTrendChart data={profile.alertTrend} />
            <CustomerBehaviorSummary behavior={profile.behavior} />
          </div>
          <div className="grid gap-6 lg:grid-cols-2">
            <CustomerDevicesTable devices={profile.behavior.devicesUsed} />
            <CustomerCountriesTable countries={profile.behavior.countriesUsed} />
          </div>
          <LinkedCustomersCard customerId={customerId} />
          <div className="grid gap-6 lg:grid-cols-3">
            <CustomerRecentTransactions transactions={profile.recentTransactions} />
            <CustomerAlertHistory alerts={profile.recentAlerts} />
            <CustomerCaseHistory cases={profile.recentCases} />
          </div>
        </div>
      )}
    </div>
  );
}
