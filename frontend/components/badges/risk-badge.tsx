import { Badge } from "@/components/ui/badge";
import type { RiskLevel } from "@/types";

const RISK_VARIANT: Record<RiskLevel, "success" | "warning" | "destructive" | "info"> = {
  LOW: "success",
  MEDIUM: "info",
  HIGH: "warning",
  CRITICAL: "destructive",
};

export function RiskBadge({ level }: { level: RiskLevel }) {
  return <Badge variant={RISK_VARIANT[level]}>{level}</Badge>;
}
