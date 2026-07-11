"use client";

import { useState } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { Pencil } from "lucide-react";
import { DataTable } from "@/components/tables/data-table";
import { RiskBadge } from "@/components/badges/risk-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogClose, DialogFooter } from "@/components/ui/dialog";
import type { AlertSlaPolicy, AlertSlaPolicyPayload } from "@/types";

interface SlaPolicyTableProps {
  policies: AlertSlaPolicy[];
  canManage: boolean;
  onUpdate: (policyId: string, payload: AlertSlaPolicyPayload) => Promise<void>;
}

export function SlaPolicyTable({ policies, canManage, onUpdate }: SlaPolicyTableProps) {
  const [editingPolicy, setEditingPolicy] = useState<AlertSlaPolicy | null>(null);

  const columns: ColumnDef<AlertSlaPolicy>[] = [
    { accessorKey: "policyId", header: "Policy ID" },
    { accessorKey: "priority", header: "Priority", cell: ({ row }) => <RiskBadge level={row.original.priority} /> },
    { accessorKey: "responseTimeMinutes", header: "Response Time (min)" },
    { accessorKey: "resolutionTimeMinutes", header: "Resolution Time (min)" },
    {
      accessorKey: "enabled",
      header: "Status",
      cell: ({ row }) => <Badge variant={row.original.enabled ? "success" : "neutral"}>{row.original.enabled ? "Enabled" : "Disabled"}</Badge>,
    },
    ...(canManage
      ? [
          {
            id: "actions",
            header: "Actions",
            cell: ({ row }: { row: { original: AlertSlaPolicy } }) => (
              <Button variant="ghost" size="icon" aria-label="Edit policy" onClick={() => setEditingPolicy(row.original)}>
                <Pencil className="h-3.5 w-3.5" />
              </Button>
            ),
          } as ColumnDef<AlertSlaPolicy>,
        ]
      : []),
  ];

  return (
    <>
      <DataTable columns={columns} data={policies} searchPlaceholder="Search SLA policies..." emptyMessage="No SLA policies found." />
      <SlaPolicyForm policy={editingPolicy} onOpenChange={(open) => !open && setEditingPolicy(null)} onSave={onUpdate} />
    </>
  );
}

function SlaPolicyForm({
  policy,
  onOpenChange,
  onSave,
}: {
  policy: AlertSlaPolicy | null;
  onOpenChange: (open: boolean) => void;
  onSave: (policyId: string, payload: AlertSlaPolicyPayload) => Promise<void>;
}) {
  const [responseTimeMinutes, setResponseTimeMinutes] = useState(policy?.responseTimeMinutes ?? 0);
  const [resolutionTimeMinutes, setResolutionTimeMinutes] = useState(policy?.resolutionTimeMinutes ?? 0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!policy) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!policy) return;
    setIsSubmitting(true);
    try {
      await onSave(policy.policyId, { responseTimeMinutes, resolutionTimeMinutes, enabled: policy.enabled });
      onOpenChange(false);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <Dialog open={!!policy} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit SLA Policy - {policy.priority}</DialogTitle>
          <DialogClose onClick={() => onOpenChange(false)} />
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="mb-1 block text-xs font-medium text-muted-foreground">Response Time (minutes)</label>
            <Input
              type="number"
              value={responseTimeMinutes}
              onChange={(e) => setResponseTimeMinutes(Number(e.target.value))}
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-muted-foreground">Resolution Time (minutes)</label>
            <Input
              type="number"
              value={resolutionTimeMinutes}
              onChange={(e) => setResolutionTimeMinutes(Number(e.target.value))}
              required
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Saving..." : "Save Changes"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
