"use client";

import { useMemo } from "react";
import Link from "next/link";
import type { ColumnDef } from "@tanstack/react-table";
import { Eye } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { CustomerStatusBadge } from "@/components/badges/customer-status-badge";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { formatDate } from "@/lib/formatters";
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
        cell: ({ getValue }) => <CustomerStatusBadge status={getValue() as never} />,
      },
      { accessorKey: "createdAt", header: "Created", cell: ({ getValue }) => formatDate(getValue() as string) },
      {
        id: "actions",
        header: "Actions",
        cell: ({ row }) => (
          <Link
            href={`/customers/${row.original.customerId}`}
            aria-label="View risk profile"
            className={cn(buttonVariants({ variant: "ghost", size: "icon" }))}
          >
            <Eye className="h-3.5 w-3.5" />
          </Link>
        ),
      },
    ],
    [],
  );

  return <DataTable columns={columns} data={customers} searchPlaceholder="Search customers..." emptyMessage="No customers found." />;
}
