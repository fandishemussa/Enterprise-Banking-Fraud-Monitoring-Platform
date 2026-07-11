import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { formatDate } from "@/lib/formatters";
import type { CustomerBehaviorSummary as CustomerBehaviorSummaryType } from "@/types";

export function CustomerBehaviorSummary({ behavior }: { behavior: CustomerBehaviorSummaryType }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Behavior Summary</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3 text-sm">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <p className="text-xs text-muted-foreground">Most Used Channel</p>
            <p className="font-medium">{behavior.mostUsedChannel ?? "-"}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Most Used Country</p>
            <p className="font-medium">{behavior.mostUsedCountry ?? "-"}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Last Transaction</p>
            <p className="font-medium">{formatDate(behavior.lastTransactionAt)}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Last Alert</p>
            <p className="font-medium">{formatDate(behavior.lastAlertAt)}</p>
          </div>
        </div>
        {behavior.highRiskMerchantCategoriesUsed.length > 0 && (
          <div>
            <p className="mb-1 text-xs text-muted-foreground">High-Risk Merchant Categories Used</p>
            <div className="flex flex-wrap gap-1">
              {behavior.highRiskMerchantCategoriesUsed.map((category) => (
                <Badge key={category} variant="destructive">
                  {category}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
