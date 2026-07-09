"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { formatDate } from "@/lib/formatters";
import type { RiskScore } from "@/types";

export function RiskScoresTable({ riskScores }: { riskScores: RiskScore[] }) {
  const columns = useMemo<ColumnDef<RiskScore>[]>(
    () => [
      { accessorKey: "transactionId", header: "Transaction ID" },
      { accessorKey: "ruleScore", header: "Rule Score" },
      { accessorKey: "mlScore", header: "ML Score", cell: ({ getValue }) => (getValue() ?? "-") },
      { accessorKey: "finalScore", header: "Final Score" },
      { accessorKey: "riskLevel", header: "Risk Level", cell: ({ getValue }) => <RiskBadge level={getValue() as never} /> },
      { accessorKey: "scoringSource", header: "Source", cell: ({ getValue }) => (getValue() as string) ?? "RULE_BASED" },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return <DataTable columns={columns} data={riskScores} searchPlaceholder="Search risk scores..." emptyMessage="No risk scores found." />;
}
