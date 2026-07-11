import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { CustomerStatus } from "@/types";

const STATUS_VARIANT: Record<CustomerStatus, "success" | "warning" | "destructive" | "neutral"> = {
  ACTIVE: "success",
  SUSPENDED: "warning",
  LOCKED: "destructive",
  CLOSED: "neutral",
};

export function CustomerStatusBadge({ status }: { status: CustomerStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
