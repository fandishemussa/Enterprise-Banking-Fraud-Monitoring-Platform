import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { RiskBadge } from "@/components/badges/risk-badge";
import { AlertStatusBadge } from "@/components/badges/alert-status-badge";
import { formatDate } from "@/lib/formatters";
import type { FraudAlert } from "@/types";

export function LinkedAlertCard({ alert }: { alert: FraudAlert | null }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Linked Alert</CardTitle>
      </CardHeader>
      <CardContent>
        {!alert ? (
          <p className="text-sm text-muted-foreground">Alert details unavailable.</p>
        ) : (
          <div className="space-y-2 text-sm">
            <div className="flex items-center justify-between">
              <span className="font-medium">{alert.alertId}</span>
              <div className="flex gap-2">
                <RiskBadge level={alert.priority} />
                <AlertStatusBadge status={alert.status} />
              </div>
            </div>
            <p className="text-muted-foreground">{alert.message ?? "No message recorded"}</p>
            <p className="text-xs text-muted-foreground">Created {formatDate(alert.createdAt)}</p>
            <Link href="/alerts" className="text-xs font-medium text-primary hover:underline">
              View all alerts
            </Link>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
