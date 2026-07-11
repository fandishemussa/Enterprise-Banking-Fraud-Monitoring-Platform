import { Badge } from "@/components/ui/badge";

export function FraudRuleStatusBadge({ enabled }: { enabled: boolean }) {
  return <Badge variant={enabled ? "success" : "neutral"}>{enabled ? "Enabled" : "Disabled"}</Badge>;
}
