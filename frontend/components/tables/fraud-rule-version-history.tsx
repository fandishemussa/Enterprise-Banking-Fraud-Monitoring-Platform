"use client";

import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose } from "@/components/ui/dialog";
import { getFraudRuleVersions } from "@/lib/api";
import { formatDate } from "@/lib/formatters";
import type { FraudRule, FraudRuleVersion } from "@/types";

interface FraudRuleVersionHistoryProps {
  rule: FraudRule | null;
  onOpenChange: (open: boolean) => void;
}

export function FraudRuleVersionHistory({ rule, onOpenChange }: FraudRuleVersionHistoryProps) {
  const [versions, setVersions] = useState<FraudRuleVersion[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!rule) return;
    setIsLoading(true);
    getFraudRuleVersions(rule.ruleId)
      .then((result) => setVersions(result.data))
      .finally(() => setIsLoading(false));
  }, [rule]);

  if (!rule) return null;

  return (
    <Dialog open={!!rule} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Version History - {rule.name}</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading...</p>
        ) : versions.length === 0 ? (
          <p className="text-sm text-muted-foreground">No changes recorded yet.</p>
        ) : (
          <div className="max-h-96 space-y-3 overflow-y-auto">
            {versions.map((version) => (
              <div key={version.versionNumber} className="rounded-md border border-border p-3 text-sm">
                <div className="flex items-center justify-between">
                  <span className="font-semibold">v{version.versionNumber}</span>
                  <span className="text-xs text-muted-foreground">{formatDate(version.createdAt)}</span>
                </div>
                <p className="mt-1 text-muted-foreground">
                  {version.changeReason ?? "No reason provided"} - by {version.changedBy}
                </p>
                <pre className="mt-2 overflow-x-auto rounded bg-muted p-2 text-xs">{version.newConfigJson}</pre>
              </div>
            ))}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
