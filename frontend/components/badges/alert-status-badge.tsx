import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { AlertStatus } from "@/types";

const STATUS_VARIANT: Record<AlertStatus, "info" | "warning" | "destructive" | "success" | "neutral"> = {
  OPEN: "info",
  ACKNOWLEDGED: "warning",
  INVESTIGATING: "warning",
  FALSE_POSITIVE: "neutral",
  CONFIRMED_FRAUD: "destructive",
  RESOLVED: "success",
  ESCALATED: "destructive",
};

export function AlertStatusBadge({ status }: { status: AlertStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
