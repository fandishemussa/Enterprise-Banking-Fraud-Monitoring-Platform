"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { formatDate, titleCase } from "@/lib/formatters";
import type { AuditLog } from "@/types";

export function AuditLogsTable({ logs, initialFilter }: { logs: AuditLog[]; initialFilter?: string }) {
  const columns = useMemo<ColumnDef<AuditLog>[]>(
    () => [
      {
        accessorKey: "eventType",
        header: "Event Type",
        cell: ({ getValue }) => <Badge variant="info">{titleCase(getValue() as string)}</Badge>,
      },
      { accessorKey: "username", header: "User" },
      { accessorKey: "role", header: "Role", cell: ({ getValue }) => (getValue() as string) ?? "-" },
      { accessorKey: "entityType", header: "Entity Type" },
      { accessorKey: "entityId", header: "Entity ID" },
      { accessorKey: "description", header: "Description" },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return (
    <DataTable
      columns={columns}
      data={logs}
      searchPlaceholder="Search audit logs..."
      emptyMessage="No audit log entries found."
      initialGlobalFilter={initialFilter}
    />
  );
}
