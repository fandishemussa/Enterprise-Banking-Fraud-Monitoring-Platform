"use client";

import type { ColumnDef } from "@tanstack/react-table";
import Link from "next/link";
import { KeyRound, Ban, CheckCircle2, Pencil, ScrollText } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { Badge } from "@/components/ui/badge";
import { Button, buttonVariants } from "@/components/ui/button";
import { formatDate } from "@/lib/formatters";
import { cn } from "@/lib/utils";
import type { AppUser, UserStatus } from "@/types";

const STATUS_VARIANT: Record<UserStatus, "success" | "destructive" | "warning"> = {
  ACTIVE: "success",
  DISABLED: "destructive",
  LOCKED: "warning",
};

interface UsersTableProps {
  users: AppUser[];
  currentUsername: string;
  onEdit: (user: AppUser) => void;
  onResetPassword: (user: AppUser) => void;
  onToggleStatus: (user: AppUser) => void;
}

export function UsersTable({ users, currentUsername, onEdit, onResetPassword, onToggleStatus }: UsersTableProps) {
  const columns: ColumnDef<AppUser>[] = [
    { accessorKey: "userId", header: "User ID" },
    { accessorKey: "fullName", header: "Full Name" },
    { accessorKey: "username", header: "Username" },
    { accessorKey: "email", header: "Email" },
    {
      accessorKey: "role",
      header: "Role",
      cell: ({ row }) => <Badge variant="info">{row.original.role}</Badge>,
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ row }) => <Badge variant={STATUS_VARIANT[row.original.status]}>{row.original.status}</Badge>,
    },
    {
      accessorKey: "lastLoginAt",
      header: "Last Login",
      cell: ({ row }) => (row.original.lastLoginAt ? formatDate(row.original.lastLoginAt) : "Never"),
    },
    {
      id: "actions",
      header: "Actions",
      cell: ({ row }) => {
        const user = row.original;
        const isSelf = user.username === currentUsername;
        return (
          <div className="flex items-center gap-1">
            <Button variant="ghost" size="icon" aria-label="Edit user" onClick={() => onEdit(user)}>
              <Pencil className="h-3.5 w-3.5" />
            </Button>
            <Button variant="ghost" size="icon" aria-label="Reset password" onClick={() => onResetPassword(user)}>
              <KeyRound className="h-3.5 w-3.5" />
            </Button>
            <Link
              href={`/audit-logs?entityId=${encodeURIComponent(user.userId)}`}
              aria-label="View audit trail"
              className={cn(buttonVariants({ variant: "ghost", size: "icon" }))}
            >
              <ScrollText className="h-3.5 w-3.5" />
            </Link>
            <Button
              variant="ghost"
              size="icon"
              aria-label={user.status === "ACTIVE" ? "Disable user" : "Activate user"}
              disabled={isSelf}
              title={isSelf ? "You cannot disable your own account" : undefined}
              onClick={() => onToggleStatus(user)}
            >
              {user.status === "ACTIVE" ? (
                <Ban className="h-3.5 w-3.5 text-destructive" />
              ) : (
                <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
              )}
            </Button>
          </div>
        );
      },
    },
  ];

  return <DataTable columns={columns} data={users} searchPlaceholder="Search users..." emptyMessage="No users found." />;
}
