"use client";

import { useMemo } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { TransactionStatusBadge } from "@/components/badges/transaction-status-badge";
import { formatCurrency, formatDate, titleCase } from "@/lib/formatters";
import type { BankingTransaction } from "@/types";

export function TransactionsTable({
  transactions,
  initialSearch,
}: {
  transactions: BankingTransaction[];
  initialSearch?: string;
}) {
  const columns = useMemo<ColumnDef<BankingTransaction>[]>(
    () => [
      { accessorKey: "transactionId", header: "Transaction ID" },
      { accessorKey: "customerId", header: "Customer" },
      { accessorKey: "sourceAccountId", header: "Source Account" },
      { accessorKey: "destinationAccountId", header: "Destination", cell: ({ getValue }) => (getValue() as string) ?? "-" },
      {
        accessorKey: "amount",
        header: "Amount",
        cell: ({ row }) => formatCurrency(row.original.amount, row.original.currency),
      },
      { accessorKey: "transactionType", header: "Type", cell: ({ getValue }) => titleCase(getValue() as string) },
      { accessorKey: "channel", header: "Channel", cell: ({ getValue }) => titleCase(getValue() as string) },
      { accessorKey: "country", header: "Country" },
      { accessorKey: "status", header: "Status", cell: ({ getValue }) => <TransactionStatusBadge status={getValue() as never} /> },
      {
        accessorKey: "riskLevel",
        header: "Risk",
        cell: ({ getValue }) => (getValue() ? <RiskBadge level={getValue() as never} /> : "-"),
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
    ],
    [],
  );

  return (
    <DataTable
      columns={columns}
      data={transactions}
      searchPlaceholder="Search transactions..."
      emptyMessage="No transactions found."
      initialGlobalFilter={initialSearch}
    />
  );
}
