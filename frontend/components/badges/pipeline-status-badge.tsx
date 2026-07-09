import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/formatters";
import type { PipelineStatus } from "@/types";

const STATUS_VARIANT: Record<PipelineStatus, "info" | "success" | "destructive" | "warning"> = {
  RUNNING: "info",
  SUCCESS: "success",
  FAILED: "destructive",
  PARTIAL_SUCCESS: "warning",
};

export function PipelineStatusBadge({ status }: { status: PipelineStatus }) {
  return <Badge variant={STATUS_VARIANT[status]}>{titleCase(status)}</Badge>;
}
