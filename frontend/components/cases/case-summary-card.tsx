import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { RiskBadge } from "@/components/badges/risk-badge";
import { CaseStatusBadge } from "@/components/badges/case-status-badge";
import { Badge } from "@/components/ui/badge";
import { formatDate, titleCase } from "@/lib/formatters";
import type { InvestigationCase } from "@/types";

export function CaseSummaryCard({ investigationCase }: { investigationCase: InvestigationCase }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Case {investigationCase.caseId}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="flex flex-wrap items-center gap-2">
          <CaseStatusBadge status={investigationCase.status} />
          <RiskBadge level={investigationCase.priority} />
          <Badge variant="neutral">{titleCase(investigationCase.decision)}</Badge>
        </div>
        <dl className="grid grid-cols-2 gap-3 text-sm">
          <div>
            <dt className="text-muted-foreground">Assigned To</dt>
            <dd className="font-medium">{investigationCase.assignedTo ?? "Unassigned"}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Customer</dt>
            <dd className="font-medium">{investigationCase.customerId}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Created</dt>
            <dd className="font-medium">{formatDate(investigationCase.createdAt)}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground">Last Updated</dt>
            <dd className="font-medium">{formatDate(investigationCase.updatedAt)}</dd>
          </div>
          {investigationCase.closedAt && (
            <div>
              <dt className="text-muted-foreground">Closed</dt>
              <dd className="font-medium">{formatDate(investigationCase.closedAt)}</dd>
            </div>
          )}
        </dl>
        {investigationCase.notes && (
          <p className="rounded-md bg-muted p-3 text-sm text-muted-foreground">{investigationCase.notes}</p>
        )}
      </CardContent>
    </Card>
  );
}
