"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { formatCurrency, formatDate, titleCase } from "@/lib/formatters";
import type { Account } from "@/types";

export function AccountsTable({ accounts }: { accounts: Account[] }) {
  const columns = useMemo<ColumnDef<Account>[]>(
    () => [
      { accessorKey: "accountId", header: "Account ID" },
      { accessorKey: "customerId", header: "Customer" },
      { accessorKey: "accountType", header: "Type", cell: ({ getValue }) => titleCase(getValue() as string) },
      {
        accessorKey: "balance",
        header: "Balance",
        cell: ({ row }) => formatCurrency(row.original.balance, row.original.currency),
      },
      { accessorKey: "currency", header: "Currency" },
      {
        accessorKey: "status",
        header: "Status",
        cell: ({ getValue }) => (
          <Badge variant={getValue() === "ACTIVE" ? "success" : getValue() === "BLOCKED" ? "destructive" : "neutral"}>
            {titleCase(getValue() as string)}
          </Badge>
        ),
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return <DataTable columns={columns} data={accounts} searchPlaceholder="Search accounts..." emptyMessage="No accounts found." />;
}
