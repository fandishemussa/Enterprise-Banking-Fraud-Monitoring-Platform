"use client";

import { useMemo, useState } from "react";
import { Plus } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { FraudRulesTable } from "@/components/tables/fraud-rules-table";
import { FraudRuleForm } from "@/components/tables/fraud-rule-form";
import { CreateFraudRuleForm } from "@/components/tables/create-fraud-rule-form";
import { FraudRuleVersionHistory } from "@/components/tables/fraud-rule-version-history";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { useFraudRules } from "@/hooks/use-fraud-rules";
import { useAuth } from "@/hooks/use-auth";
import type { FraudRule, FraudRuleSeverity } from "@/types";

const SEVERITY_OPTIONS: (FraudRuleSeverity | "ALL")[] = ["ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL"];

export default function RiskRulesPage() {
  const { hasRole } = useAuth();
  const canManage = hasRole("ADMIN");
  const { data: rules, isLoading, error, usingMockData, refetch, setEnabled } = useFraudRules();

  const [severityFilter, setSeverityFilter] = useState<FraudRuleSeverity | "ALL">("ALL");
  const [statusFilter, setStatusFilter] = useState<"ALL" | "ENABLED" | "DISABLED">("ALL");
  const [editingRule, setEditingRule] = useState<FraudRule | null>(null);
  const [historyRule, setHistoryRule] = useState<FraudRule | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const filteredRules = useMemo(() => {
    return (rules ?? []).filter((rule) => {
      if (severityFilter !== "ALL" && rule.severity !== severityFilter) return false;
      if (statusFilter === "ENABLED" && !rule.enabled) return false;
      if (statusFilter === "DISABLED" && rule.enabled) return false;
      return true;
    });
  }, [rules, severityFilter, statusFilter]);

  async function handleToggleEnabled(rule: FraudRule) {
    setActionError(null);
    try {
      await setEnabled(rule.ruleId, !rule.enabled);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "Failed to update rule status");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Risk Rules"
        description="Configurable fraud detection rules that drive the rule-based portion of the risk score"
        action={
          canManage && (
            <Button onClick={() => setCreateOpen(true)}>
              <Plus className="mr-2 h-4 w-4" />
              Add Rule
            </Button>
          )
        }
      />
      {usingMockData && <DemoDataBanner />}
      {actionError && <p className="text-sm text-destructive">{actionError}</p>}

      <div className="flex flex-wrap items-center gap-3">
        <div className="w-48">
          <Select value={severityFilter} onChange={(e) => setSeverityFilter(e.target.value as FraudRuleSeverity | "ALL")}>
            {SEVERITY_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s === "ALL" ? "All severities" : s}
              </option>
            ))}
          </Select>
        </div>
        <div className="w-48">
          <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as "ALL" | "ENABLED" | "DISABLED")}>
            <option value="ALL">All statuses</option>
            <option value="ENABLED">Enabled only</option>
            <option value="DISABLED">Disabled only</option>
          </Select>
        </div>
      </div>

      {isLoading ? (
        <LoadingState label="Loading fraud rules..." />
      ) : error ? (
        <ErrorState message={error} onRetry={refetch} />
      ) : (
        <FraudRulesTable
          rules={filteredRules}
          canManage={canManage}
          onEdit={setEditingRule}
          onToggleEnabled={handleToggleEnabled}
          onViewHistory={setHistoryRule}
        />
      )}

      <FraudRuleForm rule={editingRule} onOpenChange={(open) => !open && setEditingRule(null)} onSaved={refetch} />
      <FraudRuleVersionHistory rule={historyRule} onOpenChange={(open) => !open && setHistoryRule(null)} />
      {canManage && <CreateFraudRuleForm open={createOpen} onOpenChange={setCreateOpen} onCreated={refetch} />}
    </div>
  );
}
