"use client";

import { useEffect, useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { updateFraudRule } from "@/lib/api";
import type { FraudRule, FraudRuleSeverity } from "@/types";

const SEVERITIES: FraudRuleSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

interface FraudRuleFormProps {
  rule: FraudRule | null;
  onOpenChange: (open: boolean) => void;
  onSaved: () => void;
}

/** Edit-only (rule creation happens via the seeder/backend for the six built-in rules; this form
 * covers the "edit thresholdValue/scoreImpact/severity" workflow called for in the feature spec). */
export function FraudRuleForm({ rule, onOpenChange, onSaved }: FraudRuleFormProps) {
  const [thresholdValue, setThresholdValue] = useState("");
  const [secondaryThresholdValue, setSecondaryThresholdValue] = useState("");
  const [scoreImpact, setScoreImpact] = useState(0);
  const [secondaryScoreImpact, setSecondaryScoreImpact] = useState("");
  const [severity, setSeverity] = useState<FraudRuleSeverity>("MEDIUM");
  const [changeReason, setChangeReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!rule) return;
    setThresholdValue(rule.thresholdValue ?? "");
    setSecondaryThresholdValue(rule.secondaryThresholdValue ?? "");
    setScoreImpact(rule.scoreImpact);
    setSecondaryScoreImpact(rule.secondaryScoreImpact != null ? String(rule.secondaryScoreImpact) : "");
    setSeverity(rule.severity);
    setChangeReason("");
    setError(null);
  }, [rule]);

  if (!rule) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!rule) return;
    setError(null);
    setIsSubmitting(true);
    try {
      await updateFraudRule(rule.ruleId, {
        ruleCode: rule.ruleCode,
        name: rule.name,
        description: rule.description ?? undefined,
        category: rule.category ?? undefined,
        enabled: rule.enabled,
        thresholdValue: thresholdValue || undefined,
        secondaryThresholdValue: secondaryThresholdValue || undefined,
        scoreImpact,
        secondaryScoreImpact: secondaryScoreImpact ? Number(secondaryScoreImpact) : undefined,
        severity,
        ruleType: rule.ruleType,
        changeReason: changeReason || undefined,
      });
      onOpenChange(false);
      onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update fraud rule");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={!!rule} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Fraud Rule - {rule.name}</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-3">
          <p className="text-sm text-muted-foreground">{rule.description}</p>
          <div className="grid grid-cols-2 gap-3">
            <Input
              placeholder="Threshold value"
              value={thresholdValue}
              onChange={(e) => setThresholdValue(e.target.value)}
            />
            <Input
              placeholder="Secondary threshold value"
              value={secondaryThresholdValue}
              onChange={(e) => setSecondaryThresholdValue(e.target.value)}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              placeholder="Score impact"
              value={scoreImpact}
              onChange={(e) => setScoreImpact(Number(e.target.value))}
              required
            />
            <Input
              type="number"
              placeholder="Secondary score impact"
              value={secondaryScoreImpact}
              onChange={(e) => setSecondaryScoreImpact(e.target.value)}
            />
          </div>
          <Select value={severity} onChange={(e) => setSeverity(e.target.value as FraudRuleSeverity)}>
            {SEVERITIES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Select>
          <Input
            placeholder="Reason for change (optional, recorded in version history)"
            value={changeReason}
            onChange={(e) => setChangeReason(e.target.value)}
          />
          {error && <p className="text-sm text-destructive">{error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : "Save Changes"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
