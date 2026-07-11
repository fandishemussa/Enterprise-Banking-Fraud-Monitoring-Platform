import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { SlaStatus } from "@/types";

const STATUS_VARIANT: Record<SlaStatus, "success" | "warning" | "destructive" | "neutral"> = {
  ON_TRACK: "success",
  NEAR_BREACH: "warning",
  BREACHED: "destructive",
  COMPLETED: "neutral",
};

export function SlaStatusBadge({ status }: { status: SlaStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
