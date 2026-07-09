"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { RotateCcw, XCircle } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { formatDate, titleCase } from "@/lib/formatters";
import type { DeadLetterStatus, DeadLetterTransaction } from "@/types";

const STATUS_VARIANT: Record<DeadLetterStatus, "info" | "warning" | "success" | "neutral"> = {
  NEW: "info",
  RETRIED: "warning",
  IGNORED: "neutral",
  RESOLVED: "success",
};

export function DeadLetterTable({
  records,
  onRetry,
  onIgnore,
}: {
  records: DeadLetterTransaction[];
  onRetry?: (id: string) => Promise<unknown>;
  onIgnore?: (id: string) => Promise<unknown>;
}) {
  const columns = useMemo<ColumnDef<DeadLetterTransaction>[]>(
    () => [
      { accessorKey: "eventId", header: "Event ID" },
      { accessorKey: "errorType", header: "Error Type" },
      { accessorKey: "errorReason", header: "Error Reason" },
      {
        accessorKey: "processedStatus",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={STATUS_VARIANT[getValue() as DeadLetterStatus]}>{titleCase(getValue() as string)}</Badge>
        ),
      },
      { accessorKey: "receivedAt", header: "Received", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "",
        cell: ({ row }) => (
          <div className="flex gap-1.5">
            {onRetry && (
              <Button variant="outline" size="sm" onClick={() => onRetry(row.original.eventId)}>
                <RotateCcw className="mr-1.5 h-3.5 w-3.5" />
                Retry
              </Button>
            )}
            {onIgnore && (
              <Button variant="ghost" size="sm" onClick={() => onIgnore(row.original.eventId)}>
                <XCircle className="mr-1.5 h-3.5 w-3.5" />
                Ignore
              </Button>
            )}
          </div>
        ),
      },
    ],
    [onRetry, onIgnore],
  );

  return <DataTable columns={columns} data={records} searchPlaceholder="Search dead letter records..." emptyMessage="No dead letter records found." />;
}
