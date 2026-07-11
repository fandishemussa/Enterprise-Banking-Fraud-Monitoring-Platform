import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CaseStatusBadge } from "@/components/badges/case-status-badge";
import { Badge } from "@/components/ui/badge";
import { formatDate, titleCase } from "@/lib/formatters";
import type { InvestigationCase } from "@/types";

export function CustomerCaseHistory({ cases }: { cases: InvestigationCase[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Case History</CardTitle>
      </CardHeader>
      <CardContent>
        {cases.length === 0 ? (
          <p className="text-sm text-muted-foreground">No investigation cases recorded.</p>
        ) : (
          <div className="space-y-2">
            {cases.map((c) => (
              <Link
                key={c.caseId}
                href={`/cases/${c.caseId}`}
                className="flex items-center justify-between rounded-md border border-border p-2 text-sm hover:bg-accent"
              >
                <div>
                  <p className="font-medium">{c.caseId}</p>
                  <p className="text-xs text-muted-foreground">{formatDate(c.createdAt)}</p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant="neutral">{titleCase(c.decision)}</Badge>
                  <CaseStatusBadge status={c.status} />
                </div>
              </Link>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
