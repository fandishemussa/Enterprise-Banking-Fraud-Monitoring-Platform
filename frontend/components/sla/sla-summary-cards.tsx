import { CheckCircle2, AlertTriangle, ShieldAlert, Timer } from "lucide-react";
import { MetricCard } from "@/components/dashboard/metric-card";
import { formatPercent } from "@/lib/formatters";
import type { SlaSummary } from "@/types";

export function SlaSummaryCards({ summary }: { summary: SlaSummary }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard
        label="SLA Compliance Rate"
        value={formatPercent(summary.slaComplianceRatePercent)}
        hint={`${summary.completed} completed`}
        icon={CheckCircle2}
        tone={summary.slaComplianceRatePercent >= 90 ? "success" : summary.slaComplianceRatePercent >= 70 ? "warning" : "destructive"}
      />
      <MetricCard
        label="Breached"
        value={String(summary.breached)}
        hint="Past response or resolution deadline"
        icon={ShieldAlert}
        tone={summary.breached > 0 ? "destructive" : "neutral"}
      />
      <MetricCard
        label="Near Breach"
        value={String(summary.nearBreach)}
        hint="Approaching deadline"
        icon={AlertTriangle}
        tone={summary.nearBreach > 0 ? "warning" : "neutral"}
      />
      <MetricCard
        label="Avg Response / Resolution"
        value={`${Math.round(summary.averageResponseTimeMinutes)}m / ${Math.round(summary.averageResolutionTimeMinutes)}m`}
        hint={`${summary.totalAlerts} total tracked`}
        icon={Timer}
        tone="info"
      />
    </div>
  );
}
