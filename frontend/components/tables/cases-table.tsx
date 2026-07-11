"use client";

import { useMemo } from "react";
import Link from "next/link";
import type { ColumnDef } from "@tanstack/react-table";
import { Eye } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { CaseStatusBadge } from "@/components/badges/case-status-badge";
import { Badge } from "@/components/ui/badge";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { formatDate, titleCase } from "@/lib/formatters";
import type { InvestigationCase } from "@/types";

export function CasesTable({ cases }: { cases: InvestigationCase[] }) {
  const columns = useMemo<ColumnDef<InvestigationCase>[]>(
    () => [
      { accessorKey: "caseId", header: "Case ID" },
      { accessorKey: "alertId", header: "Alert" },
      { accessorKey: "customerId", header: "Customer" },
      { accessorKey: "assignedTo", header: "Assigned To", cell: ({ getValue }) => (getValue() as string) ?? "Unassigned" },
      { accessorKey: "priority", header: "Priority", cell: ({ getValue }) => <RiskBadge level={getValue() as never} /> },
      { accessorKey: "status", header: "Status", cell: ({ getValue }) => <CaseStatusBadge status={getValue() as never} /> },
      {
        accessorKey: "decision",
        header: "Decision",
        cell: ({ getValue }) => <Badge variant="neutral">{titleCase(getValue() as string)}</Badge>,
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
      { accessorKey: "updatedAt", header: "Updated", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => (
          <Link
            href={`/cases/${row.original.caseId}`}
            aria-label="View case details"
            className={cn(buttonVariants({ variant: "ghost", size: "icon" }))}
          >
            <Eye className="h-3.5 w-3.5" />
          </Link>
        ),
      },
    ],
    [],
  );

  return <DataTable columns={columns} data={cases} searchPlaceholder="Search cases..." emptyMessage="No investigation cases found." />;
}
