import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { formatDate } from "@/lib/formatters";
import type { AlertEscalation } from "@/types";

export function AlertEscalationHistory({ escalations }: { escalations: AlertEscalation[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Escalation History</CardTitle>
      </CardHeader>
      <CardContent>
        {escalations.length === 0 ? (
          <p className="text-sm text-muted-foreground">No escalations recorded yet.</p>
        ) : (
          <div className="space-y-2">
            {escalations.map((escalation) => (
              <div key={escalation.escalationId} className="rounded-md border border-border p-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="font-medium">
                    {escalation.escalatedFrom} <span className="text-muted-foreground">to</span> {escalation.escalatedTo}
                  </span>
                  <Badge variant="warning">Level {escalation.escalationLevel}</Badge>
                </div>
                {escalation.reason && <p className="mt-1 text-muted-foreground">{escalation.reason}</p>}
                <p className="mt-1 text-xs text-muted-foreground">{formatDate(escalation.createdAt)}</p>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
