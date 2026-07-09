import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { TransactionStatus } from "@/types";

const STATUS_VARIANT: Record<TransactionStatus, "success" | "destructive" | "warning" | "neutral"> = {
  SUCCESS: "success",
  FAILED: "destructive",
  PENDING: "warning",
  REVERSED: "neutral",
};

export function TransactionStatusBadge({ status }: { status: TransactionStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
