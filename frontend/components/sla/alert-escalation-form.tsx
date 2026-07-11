"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { useAssignableUsers } from "@/hooks/use-assignable-users";
import { titleCase } from "@/lib/formatters";
import type { AlertEscalationPayload } from "@/types";

interface AlertEscalationFormProps {
  alertId: string | null;
  onOpenChange: (open: boolean) => void;
  onEscalate: (alertId: string, payload: AlertEscalationPayload) => Promise<void>;
}

export function AlertEscalationForm({ alertId, onOpenChange, onEscalate }: AlertEscalationFormProps) {
  const [escalatedTo, setEscalatedTo] = useState("");
  const [reason, setReason] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { data: assignableUsers } = useAssignableUsers();

  if (!alertId) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!alertId) return;
    setError(null);
    setIsSubmitting(true);
    try {
      await onEscalate(alertId, { escalatedTo, reason: reason || undefined });
      setEscalatedTo("");
      setReason("");
      onOpenChange(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to escalate alert");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={!!alertId} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Escalate Alert {alertId}</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-3">
          <Select value={escalatedTo} onChange={(e) => setEscalatedTo(e.target.value)} required>
            <option value="">Escalate to...</option>
            {assignableUsers.map((user) => (
              <option key={user.username} value={user.username}>
                {user.fullName} ({user.username}) - {titleCase(user.role)}
              </option>
            ))}
          </Select>
          <Input placeholder="Reason (optional)" value={reason} onChange={(e) => setReason(e.target.value)} />
          {error && <p className="text-sm text-destructive">{error}</p>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting || !escalatedTo.trim()}>
              {isSubmitting ? "Escalating..." : "Escalate"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
