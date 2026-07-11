import { ArrowLeftRight, ShieldAlert, FolderSearch, Gauge } from "lucide-react";
import { MetricCard } from "@/components/dashboard/metric-card";
import { formatCurrency, formatNumber } from "@/lib/formatters";
import type { CustomerRiskProfile } from "@/types";

export function CustomerRiskKpiCards({ profile }: { profile: CustomerRiskProfile }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard
        label="Total Transactions"
        value={formatNumber(profile.totalTransactions)}
        hint={`${formatCurrency(profile.totalTransactionAmount)} total volume`}
        icon={ArrowLeftRight}
        tone="info"
      />
      <MetricCard
        label="Fraud Alerts"
        value={formatNumber(profile.totalAlerts)}
        hint={`${profile.openAlerts} open`}
        icon={ShieldAlert}
        tone={profile.openAlerts > 0 ? "warning" : "neutral"}
      />
      <MetricCard
        label="Investigation Cases"
        value={formatNumber(profile.totalCases)}
        hint={`${profile.openCases} open`}
        icon={FolderSearch}
        tone={profile.confirmedFraudCount > 0 ? "destructive" : "neutral"}
      />
      <MetricCard
        label="High/Critical Transactions"
        value={formatNumber(profile.highRiskTransactionCount + profile.criticalTransactionCount)}
        hint={`${profile.criticalTransactionCount} critical`}
        icon={Gauge}
        tone={profile.criticalTransactionCount > 0 ? "destructive" : "success"}
      />
    </div>
  );
}
