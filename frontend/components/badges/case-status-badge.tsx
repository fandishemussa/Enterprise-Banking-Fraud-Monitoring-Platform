import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { CaseStatus } from "@/types";

const STATUS_VARIANT: Record<CaseStatus, "info" | "warning" | "success" | "neutral" | "destructive"> = {
  OPEN: "info",
  IN_REVIEW: "warning",
  ESCALATED: "destructive",
  RESOLVED: "success",
  CLOSED: "neutral",
};

export function CaseStatusBadge({ status }: { status: CaseStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
