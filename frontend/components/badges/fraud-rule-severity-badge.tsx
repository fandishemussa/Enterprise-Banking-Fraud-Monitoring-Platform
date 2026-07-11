import { RiskBadge } from "@/components/badges/risk-badge";
import type { FraudRuleSeverity } from "@/types";

/** FraudRuleSeverity is a type alias of RiskLevel, so this just reuses RiskBadge's styling -
 * kept as its own component per the fraud-rules-management naming convention. */
export function FraudRuleSeverityBadge({ severity }: { severity: FraudRuleSeverity }) {
  return <RiskBadge level={severity} />;
}
