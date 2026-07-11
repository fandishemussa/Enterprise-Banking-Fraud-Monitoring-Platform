"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { RotateCcw, XCircle } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { formatDate, titleCase } from "@/lib/formatters";
import type { DeadLetterEvent, StreamingDeadLetterStatus } from "@/types";

const STATUS_VARIANT: Record<StreamingDeadLetterStatus, "info" | "warning" | "success" | "neutral"> = {
  NEW: "info",
  RETRIED: "warning",
  IGNORED: "neutral",
  RESOLVED: "success",
};

export function StreamingDeadLetterTable({
  events,
  onRetry,
  onIgnore,
}: {
  events: DeadLetterEvent[];
  onRetry?: (eventId: string) => Promise<unknown>;
  onIgnore?: (eventId: string) => Promise<unknown>;
}) {
  const columns = useMemo<ColumnDef<DeadLetterEvent>[]>(
    () => [
      { accessorKey: "eventId", header: "Event ID" },
      { accessorKey: "sourceTopic", header: "Topic" },
      { accessorKey: "errorType", header: "Error Type" },
      { accessorKey: "errorReason", header: "Error Reason" },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={STATUS_VARIANT[getValue() as StreamingDeadLetterStatus]}>{titleCase(getValue() as string)}</Badge>
        ),
      },
      { accessorKey: "createdAt", header: "Received", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => {
          const event = row.original;
          const isPending = event.status === "NEW";
          return (
            <div className="flex items-center gap-1">
              {onRetry && (
                <Button variant="ghost" size="icon" aria-label="Retry event" disabled={!isPending} onClick={() => onRetry(event.eventId)}>
                  <RotateCcw className="h-3.5 w-3.5" />
                </Button>
              )}
              {onIgnore && (
                <Button variant="ghost" size="icon" aria-label="Ignore event" disabled={!isPending} onClick={() => onIgnore(event.eventId)}>
                  <XCircle className="h-3.5 w-3.5 text-destructive" />
                </Button>
              )}
            </div>
          );
        },
      },
    ],
    [onRetry, onIgnore],
  );

  return (
    <DataTable columns={columns} data={events} searchPlaceholder="Search dead-letter events..." emptyMessage="No dead-lettered streaming events." />
  );
}
