import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AlertStatusBadge } from "@/components/badges/alert-status-badge";
import { RiskBadge } from "@/components/badges/risk-badge";
import { formatDate } from "@/lib/formatters";
import type { FraudAlert } from "@/types";

export function CustomerAlertHistory({ alerts }: { alerts: FraudAlert[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Alert History</CardTitle>
      </CardHeader>
      <CardContent>
        {alerts.length === 0 ? (
          <p className="text-sm text-muted-foreground">No alerts recorded.</p>
        ) : (
          <div className="space-y-2">
            {alerts.map((alert) => (
              <div key={alert.alertId} className="flex items-center justify-between rounded-md border border-border p-2 text-sm">
                <div>
                  <p className="font-medium">{alert.alertId}</p>
                  <p className="text-xs text-muted-foreground">{formatDate(alert.createdAt)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <RiskBadge level={alert.priority} />
                  <AlertStatusBadge status={alert.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
