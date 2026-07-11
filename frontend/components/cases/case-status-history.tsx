import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatDate } from "@/lib/formatters";
import type { CaseStatusHistory as CaseStatusHistoryType } from "@/types";

export function CaseStatusHistory({ history }: { history: CaseStatusHistoryType[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Status History</CardTitle>
      </CardHeader>
      <CardContent>
        {history.length === 0 ? (
          <p className="text-sm text-muted-foreground">No status changes recorded yet.</p>
        ) : (
          <div className="space-y-2">
            {history.map((entry, index) => (
              <div key={index} className="flex items-center justify-between rounded-md border border-border p-2 text-sm">
                <span>
                  {entry.oldStatus} <span className="text-muted-foreground">to</span> {entry.newStatus}
                </span>
                <span className="text-xs text-muted-foreground">{formatDate(entry.createdAt)}</span>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
