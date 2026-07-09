import { AlertStatusBadge } from "@/components/badges/alert-status-badge";
import { RiskBadge } from "@/components/badges/risk-badge";
import { EmptyState } from "@/components/shared/empty-state";
import { formatRelativeTime } from "@/lib/formatters";
import type { FraudAlert } from "@/types";
import { ShieldAlert } from "lucide-react";

export function RecentAlerts({ alerts }: { alerts: FraudAlert[] }) {
  if (alerts.length === 0) {
    return <EmptyState icon={ShieldAlert} title="No recent alerts" description="Fraud alerts will appear here as they're generated." />;
  }

  return (
    <ul className="divide-y divide-border">
      {alerts.map((alert) => (
        <li key={alert.alertId} className="flex items-center justify-between gap-4 py-3">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-foreground">
              {alert.alertId} &middot; {alert.transactionId}
            </p>
            <p className="truncate text-xs text-muted-foreground">{alert.message ?? alert.customerId}</p>
          </div>
          <div className="flex shrink-0 items-center gap-2">
            <RiskBadge level={alert.priority} />
            <AlertStatusBadge status={alert.status} />
            <span className="hidden w-20 shrink-0 text-right text-xs text-muted-foreground sm:block">
              {formatRelativeTime(alert.createdAt)}
            </span>
          </div>
        </li>
      ))}
    </ul>
  );
}
