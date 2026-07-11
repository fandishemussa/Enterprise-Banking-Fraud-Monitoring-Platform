"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { createFraudRule } from "@/lib/api";
import type { FraudRuleSeverity } from "@/types";

const SEVERITIES: FraudRuleSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

interface CreateFraudRuleFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreated: () => void;
}

/** Live scoring only evaluates ruleType=CUSTOM rows (via risk.rules.CustomAmountThresholdRule) -
 * the six built-in types are wired to fixed hardcoded rule classes by ruleCode, so a new row of
 * e.g. type AMOUNT would be stored but never evaluated. This form is fixed to CUSTOM for that
 * reason, and it's an amount-threshold check: enter the amount above which this rule fires. */
export function CreateFraudRuleForm({ open, onOpenChange, onCreated }: CreateFraudRuleFormProps) {
  const [ruleCode, setRuleCode] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [thresholdValue, setThresholdValue] = useState("");
  const [secondaryThresholdValue, setSecondaryThresholdValue] = useState("");
  const [scoreImpact, setScoreImpact] = useState(20);
  const [secondaryScoreImpact, setSecondaryScoreImpact] = useState("");
  const [severity, setSeverity] = useState<FraudRuleSeverity>("MEDIUM");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  function reset() {
    setRuleCode("");
    setName("");
    setDescription("");
    setCategory("");
    setThresholdValue("");
    setSecondaryThresholdValue("");
    setScoreImpact(20);
    setSecondaryScoreImpact("");
    setSeverity("MEDIUM");
    setError(null);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await createFraudRule({
        ruleCode: ruleCode.trim().toUpperCase().replace(/\s+/g, "_"),
        name,
        description: description || undefined,
        category: category || undefined,
        enabled: true,
        thresholdValue: thresholdValue || undefined,
        secondaryThresholdValue: secondaryThresholdValue || undefined,
        scoreImpact,
        secondaryScoreImpact: secondaryScoreImpact ? Number(secondaryScoreImpact) : undefined,
        severity,
        ruleType: "CUSTOM",
        changeReason: "Rule created via Risk Rules UI",
      });
      reset();
      onOpenChange(false);
      onCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create fraud rule");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add Custom Rule</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-3">
          <p className="text-sm text-muted-foreground">
            Fires when a transaction amount exceeds the threshold below. Takes effect immediately on the next
            transaction scored.
          </p>
          <div className="grid grid-cols-2 gap-3">
            <Input
              placeholder="Rule code (e.g. HIGH_VALUE_WIRE)"
              value={ruleCode}
              onChange={(e) => setRuleCode(e.target.value)}
              required
            />
            <Input placeholder="Rule name" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <Input
            placeholder="Description (optional)"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <Input placeholder="Category (optional)" value={category} onChange={(e) => setCategory(e.target.value)} />
          <div className="grid grid-cols-2 gap-3">
            <Input
              placeholder="Threshold amount"
              value={thresholdValue}
              onChange={(e) => setThresholdValue(e.target.value)}
              required
            />
            <Input
              placeholder="Secondary threshold (optional)"
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
              placeholder="Secondary score impact (optional)"
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
          {error && <p className="text-sm text-destructive">{error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creating..." : "Create Rule"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
