"use client";

import type { ColumnDef } from "@tanstack/react-table";
import { Pencil, History, Ban, CheckCircle2 } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { Button } from "@/components/ui/button";
import { FraudRuleStatusBadge } from "@/components/badges/fraud-rule-status-badge";
import { FraudRuleSeverityBadge } from "@/components/badges/fraud-rule-severity-badge";
import type { FraudRule } from "@/types";

interface FraudRulesTableProps {
  rules: FraudRule[];
  canManage: boolean;
  onEdit: (rule: FraudRule) => void;
  onToggleEnabled: (rule: FraudRule) => void;
  onViewHistory: (rule: FraudRule) => void;
}

export function FraudRulesTable({ rules, canManage, onEdit, onToggleEnabled, onViewHistory }: FraudRulesTableProps) {
  const columns: ColumnDef<FraudRule>[] = [
    { accessorKey: "ruleId", header: "Rule ID" },
    { accessorKey: "ruleCode", header: "Code" },
    { accessorKey: "name", header: "Name" },
    { accessorKey: "category", header: "Category", cell: ({ row }) => row.original.category ?? "-" },
    {
      accessorKey: "severity",
      header: "Severity",
      cell: ({ row }) => <FraudRuleSeverityBadge severity={row.original.severity} />,
    },
    {
      accessorKey: "enabled",
      header: "Status",
      cell: ({ row }) => <FraudRuleStatusBadge enabled={row.original.enabled} />,
    },
    { accessorKey: "thresholdValue", header: "Threshold", cell: ({ row }) => row.original.thresholdValue ?? "-" },
    {
      accessorKey: "secondaryThresholdValue",
      header: "Secondary Threshold",
      cell: ({ row }) => row.original.secondaryThresholdValue ?? "-",
    },
    { accessorKey: "scoreImpact", header: "Score Impact" },
    {
      accessorKey: "secondaryScoreImpact",
      header: "Secondary Score",
      cell: ({ row }) => row.original.secondaryScoreImpact ?? "-",
    },
    {
      id: "actions",
      header: "Actions",
      cell: ({ row }) => {
        const rule = row.original;
        return (
          <div className="flex items-center gap-1">
            <Button variant="ghost" size="icon" aria-label="View version history" onClick={() => onViewHistory(rule)}>
              <History className="h-3.5 w-3.5" />
            </Button>
            {canManage && (
              <>
                <Button variant="ghost" size="icon" aria-label="Edit rule" onClick={() => onEdit(rule)}>
                  <Pencil className="h-3.5 w-3.5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  aria-label={rule.enabled ? "Disable rule" : "Enable rule"}
                  onClick={() => onToggleEnabled(rule)}
                >
                  {rule.enabled ? (
                    <Ban className="h-3.5 w-3.5 text-destructive" />
                  ) : (
                    <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                  )}
                </Button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <DataTable columns={columns} data={rules} searchPlaceholder="Search rules by name or code..." emptyMessage="No fraud rules found." />
  );
}
