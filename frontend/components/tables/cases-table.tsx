"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import type { ColumnDef } from "@tanstack/react-table";
import { Eye, UserPlus } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { BulkActionBar } from "@/components/tables/bulk-action-bar";
import { RiskBadge } from "@/components/badges/risk-badge";
import { CaseStatusBadge } from "@/components/badges/case-status-badge";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogClose } from "@/components/ui/dialog";
import { useAssignableUsers } from "@/hooks/use-assignable-users";
import { cn } from "@/lib/utils";
import { formatDate, titleCase } from "@/lib/formatters";
import type { BulkOperationResponse, CaseStatus, InvestigationCase } from "@/types";

const CASE_STATUSES: CaseStatus[] = ["OPEN", "IN_REVIEW", "ESCALATED", "RESOLVED", "CLOSED"];

export function CasesTable({
  cases,
  onBulkUpdate,
}: {
  cases: InvestigationCase[];
  onBulkUpdate?: (caseIds: string[], payload: { status?: string; assignedTo?: string }) => Promise<BulkOperationResponse>;
}) {
  const { data: assignableUsers } = useAssignableUsers();
  const enableBulk = Boolean(onBulkUpdate);
  const [selectedCases, setSelectedCases] = useState<InvestigationCase[]>([]);
  const [resetSelectionKey, setResetSelectionKey] = useState(0);
  const [assignDialogOpen, setAssignDialogOpen] = useState(false);
  const [bulkAssignee, setBulkAssignee] = useState("");
  const [bulkError, setBulkError] = useState<string | null>(null);
  const [bulkSubmitting, setBulkSubmitting] = useState(false);

  function clearSelection() {
    setSelectedCases([]);
    setResetSelectionKey((k) => k + 1);
  }

  function reportBulkResult(result: BulkOperationResponse) {
    setBulkError(
      result.failures.length > 0
        ? `${result.failures.length} of ${result.failures.length + result.succeededIds.length} failed: ${result.failures[0].reason}`
        : null,
    );
  }

  async function handleBulkStatusChange(status: string) {
    if (!onBulkUpdate) return;
    setBulkSubmitting(true);
    try {
      const result = await onBulkUpdate(selectedCases.map((c) => c.caseId), { status });
      reportBulkResult(result);
      clearSelection();
    } finally {
      setBulkSubmitting(false);
    }
  }

  async function handleBulkAssignSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!onBulkUpdate || !bulkAssignee) return;
    setBulkSubmitting(true);
    try {
      const result = await onBulkUpdate(selectedCases.map((c) => c.caseId), { assignedTo: bulkAssignee });
      reportBulkResult(result);
      setAssignDialogOpen(false);
      setBulkAssignee("");
      clearSelection();
    } finally {
      setBulkSubmitting(false);
    }
  }

  const columns = useMemo<ColumnDef<InvestigationCase>[]>(
    () => [
      { accessorKey: "caseId", header: "Case ID" },
      { accessorKey: "alertId", header: "Alert" },
      { accessorKey: "customerId", header: "Customer" },
      { accessorKey: "assignedTo", header: "Assigned To", cell: ({ getValue }) => (getValue() as string) ?? "Unassigned" },
      { accessorKey: "priority", header: "Priority", cell: ({ getValue }) => <RiskBadge level={getValue() as never} /> },
      { accessorKey: "status", header: "Status", cell: ({ getValue }) => <CaseStatusBadge status={getValue() as never} /> },
      {
        accessorKey: "decision",
        header: "Decision",
        cell: ({ getValue }) => <Badge variant="neutral">{titleCase(getValue() as string)}</Badge>,
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
      { accessorKey: "updatedAt", header: "Updated", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => (
          <Link
            href={`/cases/${row.original.caseId}`}
            aria-label="View case details"
            className={cn(buttonVariants({ variant: "ghost", size: "icon" }))}
          >
            <Eye className="h-3.5 w-3.5" />
          </Link>
        ),
      },
    ],
    [],
  );

  return (
    <>
      <DataTable
        columns={columns}
        data={cases}
        searchPlaceholder="Search cases..."
        emptyMessage="No investigation cases found."
        enableRowSelection={enableBulk}
        getRowId={(c) => c.caseId}
        onSelectedRowsChange={setSelectedCases}
        resetSelectionKey={resetSelectionKey}
        bulkActionBar={
          <BulkActionBar count={selectedCases.length} onClear={clearSelection}>
            <Button size="sm" variant="outline" onClick={() => setAssignDialogOpen(true)}>
              <UserPlus className="mr-1.5 h-3.5 w-3.5" />
              Assign
            </Button>
            <Select
              className="h-8 w-40 text-xs"
              defaultValue=""
              disabled={bulkSubmitting}
              onChange={(e) => e.target.value && handleBulkStatusChange(e.target.value)}
            >
              <option value="" disabled>
                Set status...
              </option>
              {CASE_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {titleCase(status)}
                </option>
              ))}
            </Select>
          </BulkActionBar>
        }
      />
      {bulkError && <p className="mt-2 text-xs text-destructive">{bulkError}</p>}

      <Dialog open={assignDialogOpen} onOpenChange={setAssignDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Bulk Assign {selectedCases.length} Cases</DialogTitle>
            <DialogClose onClick={() => setAssignDialogOpen(false)} />
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
              <Button type="button" variant="outline" onClick={() => setAssignDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={bulkSubmitting || !bulkAssignee}>
                {bulkSubmitting ? "Assigning..." : "Assign All"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
