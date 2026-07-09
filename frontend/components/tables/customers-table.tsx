"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { Badge } from "@/components/ui/badge";
import { formatDate, titleCase } from "@/lib/formatters";
import type { Customer } from "@/types";

export function CustomersTable({ customers }: { customers: Customer[] }) {
  const columns = useMemo<ColumnDef<Customer>[]>(
    () => [
      { accessorKey: "customerId", header: "Customer ID" },
      { accessorKey: "fullName", header: "Full Name" },
      { accessorKey: "email", header: "Email" },
      { accessorKey: "country", header: "Country" },
      { accessorKey: "riskLevel", header: "Risk", cell: ({ getValue }) => <RiskBadge level={getValue() as never} /> },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={getValue() === "ACTIVE" ? "success" : getValue() === "SUSPENDED" ? "warning" : "neutral"}>
            {titleCase(getValue() as string)}
          </Badge>
        ),
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return <DataTable columns={columns} data={customers} searchPlaceholder="Search customers..." emptyMessage="No customers found." />;
}
