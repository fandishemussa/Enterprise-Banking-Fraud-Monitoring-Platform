"use client";

import { useState } from "react";
import { Lock, Unlock, ShieldAlert } from "lucide-react";
import { SectionCard } from "@/components/shared/section-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "@/hooks/use-auth";
import { useCustomerLock } from "@/hooks/use-customer-lock";
import { formatDate } from "@/lib/formatters";
import type { CustomerStatus } from "@/types";

export function CustomerLockPanel({
  customerId,
  status,
  onStatusChange,
}: {
  customerId: string;
  status: CustomerStatus;
  onStatusChange: () => void;
}) {
  const { hasRole } = useAuth();
  const isAdmin = hasRole("ADMIN");
  const isInvestigator = hasRole("INVESTIGATOR");
  const { requests, pendingRequest, requestLock, unlock } = useCustomerLock(customerId);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isAdmin && !isInvestigator) {
    return null;
  }

  async function handleLock(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await requestLock(reason);
      setReason("");
      setDialogOpen(false);
      onStatusChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit lock request");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleUnlock() {
    setError(null);
    try {
      await unlock();
      onStatusChange();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to unlock customer");
    }
  }

  return (
    <SectionCard
      title="Fraud Lock"
      description="Lock this customer to block new transactions while under investigation"
      action={
        status === "LOCKED" ? (
          isAdmin && (
            <Button variant="outline" onClick={handleUnlock}>
              <Unlock className="mr-2 h-4 w-4" />
              Unlock
            </Button>
          )
        ) : pendingRequest ? (
          <Badge variant="warning">
            <ShieldAlert className="mr-1.5 h-3.5 w-3.5" />
            Lock requested - pending admin approval
          </Badge>
        ) : (
          <Button variant="destructive" onClick={() => setDialogOpen(true)}>
            <Lock className="mr-2 h-4 w-4" />
            {isAdmin ? "Lock Customer" : "Request Lock"}
          </Button>
        )
      }
    >
      {error && <p className="mb-2 text-sm text-destructive">{error}</p>}
      {requests.length === 0 ? (
        <p className="text-sm text-muted-foreground">No lock requests for this customer.</p>
      ) : (
        <ul className="divide-y divide-border text-sm">
          {requests.map((r) => (
            <li key={r.lockRequestId} className="flex flex-col gap-1 py-2.5">
              <div className="flex items-center justify-between gap-2">
                <span className="font-medium text-foreground">{r.lockRequestId}</span>
                <Badge
                  variant={
                    r.status === "APPROVED" ? "destructive" : r.status === "REJECTED" ? "neutral" : "warning"
                  }
                >
                  {r.status}
                </Badge>
              </div>
              <p className="text-xs text-muted-foreground">
                Requested by {r.requestedBy} ({r.requestedByRole}) on {formatDate(r.createdAt)}
              </p>
              {r.reason && <p className="text-xs text-muted-foreground">Reason: {r.reason}</p>}
              {r.reviewedBy && (
                <p className="text-xs text-muted-foreground">
                  Reviewed by {r.reviewedBy} on {formatDate(r.reviewedAt)}
                  {r.reviewNotes ? ` - ${r.reviewNotes}` : ""}
                </p>
              )}
            </li>
          ))}
        </ul>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{isAdmin ? "Lock Customer" : "Request Customer Lock"}</DialogTitle>
            <DialogClose onClick={() => setDialogOpen(false)} />
          </DialogHeader>
          <form onSubmit={handleLock} className="space-y-3">
            <p className="text-sm text-muted-foreground">
              {isAdmin
                ? "This takes effect immediately - the customer will not be able to transact until unlocked."
                : "This submits a request for admin approval - the customer stays active until an admin approves it."}
            </p>
            <Input
              placeholder="Reason for lock"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              required
            />
            {error && <p className="text-sm text-destructive">{error}</p>}
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="destructive" disabled={isSubmitting}>
                {isSubmitting ? "Submitting..." : isAdmin ? "Lock Customer" : "Submit Request"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </SectionCard>
  );
}
