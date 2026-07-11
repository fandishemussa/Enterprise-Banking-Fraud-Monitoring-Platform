"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { RiskBadge } from "@/components/badges/risk-badge";
import { formatDate } from "@/lib/formatters";
import type { MlPredictionLog, RiskLevel } from "@/types";

export function RecentPredictionsTable({ predictions }: { predictions: MlPredictionLog[] }) {
  const columns = useMemo<ColumnDef<MlPredictionLog>[]>(
    () => [
      { accessorKey: "transactionId", header: "Transaction" },
      {
        accessorKey: "mlScore",
        header: "ML Score",
        cell: ({ getValue }) => (getValue() as number).toFixed(1),
      },
      {
        accessorKey: "riskLevel",
        header: "Risk Level",
        cell: ({ getValue }) => <RiskBadge level={getValue() as RiskLevel} />,
      },
      {
        accessorKey: "isAnomaly",
        header: "Anomaly",
        cell: ({ getValue }) => (getValue() ? <Badge variant="destructive">Yes</Badge> : <Badge variant="success">No</Badge>),
      },
      {
        accessorKey: "fallbackMode",
        header: "Fallback",
        cell: ({ getValue }) => (getValue() ? <Badge variant="warning">Fallback</Badge> : <Badge variant="info">Model</Badge>),
      },
      { accessorKey: "modelVersion", header: "Model Version" },
      { accessorKey: "createdAt", header: "Time", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return (
    <DataTable
      columns={columns}
      data={predictions}
      searchPlaceholder="Search predictions by transaction..."
      emptyMessage="No predictions logged yet."
    />
  );
}
