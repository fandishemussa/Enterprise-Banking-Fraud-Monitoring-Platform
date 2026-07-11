"use client";

import { useMemo, useState } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { UserPlus, ArrowUpCircle } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { BulkActionBar } from "@/components/tables/bulk-action-bar";
import { RiskBadge } from "@/components/badges/risk-badge";
import { AlertStatusBadge } from "@/components/badges/alert-status-badge";
import { AlertEscalationForm } from "@/components/sla/alert-escalation-form";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogClose } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { useAssignableUsers } from "@/hooks/use-assignable-users";
import { formatDate, titleCase } from "@/lib/formatters";
import type { AlertEscalationPayload, AlertStatus, BulkOperationResponse, FraudAlert } from "@/types";

const ALERT_STATUSES: AlertStatus[] = [
  "OPEN",
  "ACKNOWLEDGED",
  "INVESTIGATING",
  "FALSE_POSITIVE",
  "CONFIRMED_FRAUD",
  "RESOLVED",
  "ESCALATED",
];

export function AlertsTable({
  alerts,
  onChangeStatus,
  onAssign,
  onEscalate,
  onBulkAssign,
  onBulkEscalate,
  onBulkChangeStatus,
}: {
  alerts: FraudAlert[];
  onChangeStatus?: (alertId: string, status: string) => Promise<unknown>;
  onAssign?: (alertId: string, assignedTo: string) => Promise<unknown>;
  onEscalate?: (alertId: string, payload: AlertEscalationPayload) => Promise<unknown>;
  onBulkAssign?: (alertIds: string[], assignedTo: string) => Promise<BulkOperationResponse>;
  onBulkEscalate?: (alertIds: string[], escalatedTo: string, reason?: string) => Promise<BulkOperationResponse>;
  onBulkChangeStatus?: (alertIds: string[], status: string) => Promise<BulkOperationResponse>;
}) {
  const [assignTarget, setAssignTarget] = useState<FraudAlert | null>(null);
  const [assignee, setAssignee] = useState("");
  const [escalateTarget, setEscalateTarget] = useState<string | null>(null);
  const { data: assignableUsers } = useAssignableUsers();

  const enableBulk = Boolean(onBulkAssign || onBulkEscalate || onBulkChangeStatus);
  const [selectedAlerts, setSelectedAlerts] = useState<FraudAlert[]>([]);
  const [resetSelectionKey, setResetSelectionKey] = useState(0);
  const [bulkDialog, setBulkDialog] = useState<"assign" | "escalate" | null>(null);
  const [bulkAssignee, setBulkAssignee] = useState("");
  const [bulkReason, setBulkReason] = useState("");
  const [bulkError, setBulkError] = useState<string | null>(null);
  const [bulkSubmitting, setBulkSubmitting] = useState(false);

  function clearSelection() {
    setSelectedAlerts([]);
    setResetSelectionKey((k) => k + 1);
  }

  function reportBulkResult(result: BulkOperationResponse) {
    if (result.failures.length > 0) {
      setBulkError(`${result.failures.length} of ${result.failures.length + result.succeededIds.length} failed: ${result.failures[0].reason}`);
    } else {
      setBulkError(null);
    }
  }

  async function handleBulkAssignSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!onBulkAssign || !bulkAssignee) return;
    setBulkSubmitting(true);
    try {
      const result = await onBulkAssign(selectedAlerts.map((a) => a.alertId), bulkAssignee);
      reportBulkResult(result);
      setBulkDialog(null);
      setBulkAssignee("");
      clearSelection();
    } finally {
      setBulkSubmitting(false);
    }
  }

  async function handleBulkEscalateSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!onBulkEscalate || !bulkAssignee) return;
    setBulkSubmitting(true);
    try {
      const result = await onBulkEscalate(selectedAlerts.map((a) => a.alertId), bulkAssignee, bulkReason || undefined);
      reportBulkResult(result);
      setBulkDialog(null);
      setBulkAssignee("");
      setBulkReason("");
      clearSelection();
    } finally {
      setBulkSubmitting(false);
    }
  }

  async function handleBulkStatusChange(status: string) {
    if (!onBulkChangeStatus) return;
    setBulkSubmitting(true);
    try {
      const result = await onBulkChangeStatus(selectedAlerts.map((a) => a.alertId), status);
      reportBulkResult(result);
      clearSelection();
    } finally {
      setBulkSubmitting(false);
    }
  }

  const columns = useMemo<ColumnDef<FraudAlert>[]>(
    () => [
      { accessorKey: "alertId", header: "Alert ID" },
      { accessorKey: "transactionId", header: "Transaction" },
      { accessorKey: "customerId", header: "Customer" },
      { accessorKey: "priority", header: "Priority", cell: ({ getValue }) => <RiskBadge level={getValue() as never} /> },
      { accessorKey: "alertType", header: "Type", cell: ({ getValue }) => titleCase(getValue() as string) },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ row }) =>
          onChangeStatus ? (
            <Select
              defaultValue={row.original.status}
              className="h-8 w-44 text-xs"
              onChange={(event) => onChangeStatus(row.original.alertId, event.target.value)}
            >
              {ALERT_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {titleCase(status)}
                </option>
              ))}
            </Select>
          ) : (
            <AlertStatusBadge status={row.original.status} />
          ),
      },
      { accessorKey: "assignedTo", header: "Assigned To", cell: ({ getValue }) => (getValue() as string) ?? "Unassigned" },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "",
        cell: ({ row }) => (
          <div className="flex items-center gap-1">
            {onAssign && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setAssignTarget(row.original);
                  setAssignee(row.original.assignedTo ?? "");
                }}
              >
                <UserPlus className="mr-1.5 h-3.5 w-3.5" />
                Assign
              </Button>
            )}
            {onEscalate && (
              <Button variant="ghost" size="sm" onClick={() => setEscalateTarget(row.original.alertId)}>
                <ArrowUpCircle className="mr-1.5 h-3.5 w-3.5" />
                Escalate
              </Button>
            )}
          </div>
        ),
      },
    ],
    [onChangeStatus, onAssign, onEscalate],
  );

  return (
    <>
      <DataTable
        columns={columns}
        data={alerts}
        searchPlaceholder="Search alerts..."
        emptyMessage="No fraud alerts found."
        enableRowSelection={enableBulk}
        getRowId={(alert) => alert.alertId}
        onSelectedRowsChange={setSelectedAlerts}
        resetSelectionKey={resetSelectionKey}
        bulkActionBar={
          <BulkActionBar count={selectedAlerts.length} onClear={clearSelection}>
            {onBulkAssign && (
              <Button size="sm" variant="outline" onClick={() => setBulkDialog("assign")}>
                <UserPlus className="mr-1.5 h-3.5 w-3.5" />
                Assign
              </Button>
            )}
            {onBulkEscalate && (
              <Button size="sm" variant="outline" onClick={() => setBulkDialog("escalate")}>
                <ArrowUpCircle className="mr-1.5 h-3.5 w-3.5" />
                Escalate
              </Button>
            )}
            {onBulkChangeStatus && (
              <Select
                className="h-8 w-40 text-xs"
                defaultValue=""
                disabled={bulkSubmitting}
                onChange={(e) => e.target.value && handleBulkStatusChange(e.target.value)}
              >
                <option value="" disabled>
                  Set status...
                </option>
                {ALERT_STATUSES.map((status) => (
                  <option key={status} value={status}>
                    {titleCase(status)}
                  </option>
                ))}
              </Select>
            )}
          </BulkActionBar>
        }
      />
      {bulkError && <p className="mt-2 text-xs text-destructive">{bulkError}</p>}

      <Dialog open={assignTarget !== null} onOpenChange={(open) => !open && setAssignTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Assign {assignTarget?.alertId}</DialogTitle>
            <DialogClose onClick={() => setAssignTarget(null)} />
          </DialogHeader>
          <Select value={assignee} onChange={(event) => setAssignee(event.target.value)}>
            <option value="">Select a user...</option>
            {assignableUsers.map((user) => (
              <option key={user.username} value={user.username}>
                {user.fullName} ({user.username}) - {titleCase(user.role)}
              </option>
            ))}
          </Select>
          <DialogFooter>
            <Button variant="outline" onClick={() => setAssignTarget(null)}>
              Cancel
            </Button>
            <Button
              onClick={async () => {
                if (assignTarget && onAssign) await onAssign(assignTarget.alertId, assignee);
                setAssignTarget(null);
              }}
            >
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {onEscalate && (
        <AlertEscalationForm
          alertId={escalateTarget}
          onOpenChange={(open) => !open && setEscalateTarget(null)}
          onEscalate={async (alertId, payload) => {
            await onEscalate(alertId, payload);
          }}
        />
      )}

      <Dialog open={bulkDialog === "assign"} onOpenChange={(open) => !open && setBulkDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Bulk Assign {selectedAlerts.length} Alerts</DialogTitle>
            <DialogClose onClick={() => setBulkDialog(null)} />
          </DialogHeader>
          <form onSubmit={handleBulkAssignSubmit} className="space-y-3">
            <Select value={bulkAssignee} onChange={(e) => setBulkAssignee(e.target.value)} required>
              <option value="">Select a user...</option>
              {assignableUsers.map((user) => (
                <option key={user.username} value={user.username}>
                  {user.fullName} ({user.username}) - {titleCase(user.role)}
                </option>
              ))}
            </Select>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setBulkDialog(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={bulkSubmitting || !bulkAssignee}>
                {bulkSubmitting ? "Assigning..." : "Assign All"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <Dialog open={bulkDialog === "escalate"} onOpenChange={(open) => !open && setBulkDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Bulk Escalate {selectedAlerts.length} Alerts</DialogTitle>
            <DialogClose onClick={() => setBulkDialog(null)} />
          </DialogHeader>
          <form onSubmit={handleBulkEscalateSubmit} className="space-y-3">
            <Select value={bulkAssignee} onChange={(e) => setBulkAssignee(e.target.value)} required>
              <option value="">Escalate to...</option>
              {assignableUsers.map((user) => (
                <option key={user.username} value={user.username}>
                  {user.fullName} ({user.username}) - {titleCase(user.role)}
                </option>
              ))}
            </Select>
            <Input placeholder="Reason (optional)" value={bulkReason} onChange={(e) => setBulkReason(e.target.value)} />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setBulkDialog(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={bulkSubmitting || !bulkAssignee}>
                {bulkSubmitting ? "Escalating..." : "Escalate All"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
