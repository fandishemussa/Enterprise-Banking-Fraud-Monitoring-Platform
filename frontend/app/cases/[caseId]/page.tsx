"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { CaseSummaryCard } from "@/components/cases/case-summary-card";
import { LinkedAlertCard } from "@/components/cases/linked-alert-card";
import { LinkedTransactionCard } from "@/components/cases/linked-transaction-card";
import { CaseDecisionPanel } from "@/components/cases/case-decision-panel";
import { CaseTimeline } from "@/components/cases/case-timeline";
import { CaseNotesList } from "@/components/cases/case-notes-list";
import { CaseNoteForm } from "@/components/cases/case-note-form";
import { CaseStatusHistory } from "@/components/cases/case-status-history";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCaseDetail } from "@/hooks/use-case-detail";
import { useAuth } from "@/hooks/use-auth";
import { getAlerts, getRiskScores, getTransactions } from "@/lib/api";
import type { BankingTransaction, FraudAlert, RiskScore } from "@/types";

export default function CaseDetailPage({ params }: { params: Promise<{ caseId: string }> }) {
  const { caseId } = use(params);
  const { hasRole } = useAuth();
  const canWriteNotes = hasRole("ADMIN", "ANALYST", "INVESTIGATOR");
  const canManageCase = hasRole("ADMIN", "ANALYST", "INVESTIGATOR");
  const canDeleteNotes = hasRole("ADMIN");

  const {
    investigationCase,
    timeline,
    notes,
    statusHistory,
    isLoading,
    error,
    usingMockData,
    mutating,
    refetch,
    updateStatus,
    updateAssignedTo,
    updateDecision,
    addNote,
    editNote,
    removeNote,
  } = useCaseDetail(caseId);

  const [alert, setAlert] = useState<FraudAlert | null>(null);
  const [transaction, setTransaction] = useState<BankingTransaction | null>(null);
  const [riskScore, setRiskScore] = useState<RiskScore | null>(null);

  useEffect(() => {
    if (!investigationCase) return;
    getAlerts().then((result) => {
      const found = result.data.find((a) => a.alertId === investigationCase.alertId);
      setAlert(found ?? null);
    });
  }, [investigationCase]);

  useEffect(() => {
    if (!alert) return;
    getTransactions().then((result) => {
      const found = result.data.find((t) => t.transactionId === alert.transactionId);
      setTransaction(found ?? null);
    });
    getRiskScores().then((result) => {
      const found = result.data.find((r) => r.transactionId === alert.transactionId);
      setRiskScore(found ?? null);
    });
  }, [alert]);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2">
        <Link href="/cases" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Back to Cases
        </Link>
      </div>
      <PageHeader title={`Case ${caseId}`} description="Investigation timeline, notes, and decision workflow" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading case..." />
      ) : error || !investigationCase ? (
        <ErrorState message={error ?? "Case not found"} onRetry={refetch} />
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="space-y-6 lg:col-span-2">
            <CaseSummaryCard investigationCase={investigationCase} />
            <div className="grid gap-6 md:grid-cols-2">
              <LinkedAlertCard alert={alert} />
              <LinkedTransactionCard transaction={transaction} riskScore={riskScore} />
            </div>
            <Card>
              <CardHeader>
                <CardTitle>Analyst Notes</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <CaseNoteForm onSubmit={addNote} disabled={!canWriteNotes} />
                <CaseNotesList
                  notes={notes}
                  canEdit={canWriteNotes}
                  canDelete={canDeleteNotes}
                  onEdit={(noteId, noteText) => editNote(noteId, { noteText })}
                  onDelete={removeNote}
                />
              </CardContent>
            </Card>
            <CaseStatusHistory history={statusHistory} />
          </div>
          <div className="space-y-6">
            <CaseDecisionPanel
              investigationCase={investigationCase}
              canManage={canManageCase}
              mutating={mutating}
              onUpdateStatus={updateStatus}
              onUpdateAssignedTo={updateAssignedTo}
              onUpdateDecision={updateDecision}
            />
            <CaseTimeline events={timeline} />
          </div>
        </div>
      )}
    </div>
  );
}
