"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { SlaStatusBadge } from "@/components/badges/sla-status-badge";
import { formatDate } from "@/lib/formatters";
import type { AlertSlaResult } from "@/types";

export function SlaAlertsTable({ results }: { results: AlertSlaResult[] }) {
  const columns = useMemo<ColumnDef<AlertSlaResult>[]>(
    () => [
      { accessorKey: "alertId", header: "Alert ID" },
      { accessorKey: "policyId", header: "Policy" },
      { accessorKey: "responseDeadline", header: "Response Deadline", cell: ({ getValue }) => formatDate(getValue() as string) },
      { accessorKey: "resolutionDeadline", header: "Resolution Deadline", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        accessorKey: "firstAcknowledgedAt",
        header: "Acknowledged",
        cell: ({ getValue }) => (getValue() ? formatDate(getValue() as string) : "Not yet"),
      },
      {
        accessorKey: "resolvedAt",
        header: "Resolved",
        cell: ({ getValue }) => (getValue() ? formatDate(getValue() as string) : "Not yet"),
      },
      { accessorKey: "status", header: "SLA Status", cell: ({ getValue }) => <SlaStatusBadge status={getValue() as never} /> },
    ],
    [],
  );

  return <DataTable columns={columns} data={results} searchPlaceholder="Search SLA results by alert ID..." emptyMessage="No SLA-tracked alerts found." />;
}
