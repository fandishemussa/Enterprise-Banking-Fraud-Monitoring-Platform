import { RiskBadge } from "@/components/badges/risk-badge";
import { CustomerStatusBadge } from "@/components/badges/customer-status-badge";
import { formatDate } from "@/lib/formatters";
import type { CustomerRiskProfile } from "@/types";

export function CustomerRiskProfileHeader({ profile }: { profile: CustomerRiskProfile }) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4 rounded-lg border border-border bg-card p-5">
      <div>
        <h2 className="text-lg font-semibold text-foreground">{profile.fullName}</h2>
        <p className="text-sm text-muted-foreground">
          {profile.customerId} - {profile.email}
        </p>
        <p className="text-sm text-muted-foreground">{profile.country}</p>
        <p className="mt-1 text-xs text-muted-foreground">Customer since {formatDate(profile.createdAt)}</p>
      </div>
      <div className="flex items-center gap-2">
        <RiskBadge level={profile.riskLevel} />
        <CustomerStatusBadge status={profile.status} />
      </div>
    </div>
  );
}
