"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import type { FeatureDriftResult, FeatureDriftStatus } from "@/types";

const DRIFT_VARIANT: Record<FeatureDriftStatus, "success" | "warning" | "destructive"> = {
  STABLE: "success",
  WARNING: "warning",
  DRIFTED: "destructive",
};

export function FeatureDriftTable({ results }: { results: FeatureDriftResult[] }) {
  const columns = useMemo<ColumnDef<FeatureDriftResult>[]>(
    () => [
      { accessorKey: "featureName", header: "Feature" },
      {
        accessorKey: "baselineAverage",
        header: "Baseline Avg",
        cell: ({ getValue }) => (getValue() as number).toFixed(4),
      },
      {
        accessorKey: "currentAverage",
        header: "Current Avg",
        cell: ({ getValue }) => (getValue() as number).toFixed(4),
      },
      {
        accessorKey: "driftScore",
        header: "Drift Score",
        cell: ({ getValue }) => (getValue() as number).toFixed(4),
      },
      {
        accessorKey: "driftStatus",
        header: "Status",
        cell: ({ getValue }) => {
          const status = getValue() as FeatureDriftStatus;
          return <Badge variant={DRIFT_VARIANT[status]}>{status}</Badge>;
        },
      },
    ],
    [],
  );

  return (
    <DataTable
      columns={columns}
      data={results}
      searchPlaceholder="Search features..."
      emptyMessage="No drift data available yet. Train a model and score transactions to populate this table."
    />
  );
}
