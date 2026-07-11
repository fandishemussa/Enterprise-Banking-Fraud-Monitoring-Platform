"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import type { CaseDecision, CaseStatus, InvestigationCase } from "@/types";

const STATUSES: CaseStatus[] = ["OPEN", "IN_REVIEW", "ESCALATED", "RESOLVED", "CLOSED"];
const DECISIONS: CaseDecision[] = ["PENDING", "FALSE_POSITIVE", "CONFIRMED_FRAUD", "NEEDS_MORE_REVIEW"];

interface CaseDecisionPanelProps {
  investigationCase: InvestigationCase;
  canManage: boolean;
  mutating: boolean;
  onUpdateStatus: (status: string) => Promise<void>;
  onUpdateAssignedTo: (assignedTo: string) => Promise<void>;
  onUpdateDecision: (decision: string) => Promise<void>;
}

export function CaseDecisionPanel({
  investigationCase,
  canManage,
  mutating,
  onUpdateStatus,
  onUpdateAssignedTo,
  onUpdateDecision,
}: CaseDecisionPanelProps) {
  const [assignedTo, setAssignedTo] = useState(investigationCase.assignedTo ?? "");

  return (
    <Card>
      <CardHeader>
        <CardTitle>Decision & Assignment</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <label className="mb-1 block text-xs font-medium text-muted-foreground">Status</label>
          <Select
            value={investigationCase.status}
            disabled={!canManage || mutating}
            onChange={(e) => onUpdateStatus(e.target.value)}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-muted-foreground">Decision</label>
          <Select
            value={investigationCase.decision}
            disabled={!canManage || mutating}
            onChange={(e) => onUpdateDecision(e.target.value)}
          >
            {DECISIONS.map((d) => (
              <option key={d} value={d}>
                {d}
              </option>
            ))}
          </Select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-muted-foreground">Assigned To</label>
          <div className="flex gap-2">
            <Input
              value={assignedTo}
              disabled={!canManage || mutating}
              onChange={(e) => setAssignedTo(e.target.value)}
              placeholder="username"
            />
            <Button
              type="button"
              variant="outline"
              disabled={!canManage || mutating}
              onClick={() => onUpdateAssignedTo(assignedTo)}
            >
              Save
            </Button>
          </div>
        </div>
        {!canManage && <p className="text-xs text-muted-foreground">Read-only for your role.</p>}
      </CardContent>
    </Card>
  );
}
