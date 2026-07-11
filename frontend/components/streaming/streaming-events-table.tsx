"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { formatDate } from "@/lib/formatters";
import type { StreamingEventLog } from "@/types";

export function StreamingEventsTable({ events }: { events: StreamingEventLog[] }) {
  const columns = useMemo<ColumnDef<StreamingEventLog>[]>(
    () => [
      { accessorKey: "eventId", header: "Event ID" },
      { accessorKey: "topic", header: "Topic" },
      { accessorKey: "transactionId", header: "Transaction", cell: ({ getValue }) => (getValue() as string) ?? "-" },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={getValue() === "SUCCESS" ? "success" : "destructive"}>{getValue() as string}</Badge>
        ),
      },
      { accessorKey: "message", header: "Message", cell: ({ getValue }) => (getValue() as string) ?? "-" },
      { accessorKey: "createdAt", header: "Time", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return (
    <DataTable columns={columns} data={events} searchPlaceholder="Search streaming events..." emptyMessage="No streaming events yet." />
  );
}
