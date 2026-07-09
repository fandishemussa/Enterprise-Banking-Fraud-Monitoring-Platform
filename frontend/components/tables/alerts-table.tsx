"use client";

import { useMemo, useState } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { UserPlus } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { AlertStatusBadge } from "@/components/badges/alert-status-badge";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle, DialogClose } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { formatDate, titleCase } from "@/lib/formatters";
import type { AlertStatus, FraudAlert } from "@/types";

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
}: {
  alerts: FraudAlert[];
  onChangeStatus?: (alertId: string, status: string) => Promise<unknown>;
  onAssign?: (alertId: string, assignedTo: string) => Promise<unknown>;
}) {
  const [assignTarget, setAssignTarget] = useState<FraudAlert | null>(null);
  const [assignee, setAssignee] = useState("");

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
        cell: ({ row }) =>
          onAssign ? (
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
          ) : null,
      },
    ],
    [onChangeStatus, onAssign],
  );

  return (
    <>
      <DataTable columns={columns} data={alerts} searchPlaceholder="Search alerts..." emptyMessage="No fraud alerts found." />

      <Dialog open={assignTarget !== null} onOpenChange={(open) => !open && setAssignTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Assign {assignTarget?.alertId}</DialogTitle>
            <DialogClose onClick={() => setAssignTarget(null)} />
          </DialogHeader>
          <Input
            value={assignee}
            onChange={(event) => setAssignee(event.target.value)}
            placeholder="analyst.username"
          />
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
    </>
  );
}
