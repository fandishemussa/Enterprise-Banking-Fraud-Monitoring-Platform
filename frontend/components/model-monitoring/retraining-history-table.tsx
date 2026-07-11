"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { formatDate, formatNumber } from "@/lib/formatters";
import type { RetrainingHistoryEntry } from "@/types";

export function RetrainingHistoryTable({ entries }: { entries: RetrainingHistoryEntry[] }) {
  const columns = useMemo<ColumnDef<RetrainingHistoryEntry>[]>(
    () => [
      { accessorKey: "modelVersion", header: "Model Version" },
      { accessorKey: "trainingDate", header: "Trained", cell: ({ getValue }) => formatDate(getValue() as string) },
      { accessorKey: "datasetName", header: "Dataset" },
      {
        accessorKey: "numberOfTrainingRows",
        header: "Training Rows",
        cell: ({ getValue }) => formatNumber(getValue() as number),
      },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={getValue() === "SUCCESS" ? "success" : "destructive"}>{getValue() as string}</Badge>
        ),
      },
      {
        accessorKey: "metrics",
        header: "Metrics",
        cell: ({ getValue }) => {
          const metrics = getValue() as Record<string, number | null>;
          const entries = Object.entries(metrics ?? {}).filter(([, v]) => v !== null);
          if (entries.length === 0) return "-";
          return entries.map(([k, v]) => `${k}: ${(v as number).toFixed(3)}`).join(", ");
        },
      },
    ],
    [],
  );

  return (
    <DataTable
      columns={columns}
      data={entries}
      searchPlaceholder="Search by model version..."
      emptyMessage="No retraining history yet."
    />
  );
}
