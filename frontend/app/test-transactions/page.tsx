"use client";

import { useState } from "react";
import { FlaskConical, AlertCircle, ShieldAlert, CheckCircle2 } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { AccessDenied } from "@/components/shared/access-denied";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { RiskBadge } from "@/components/badges/risk-badge";
import { TransactionStatusBadge } from "@/components/badges/transaction-status-badge";
import { useAuth } from "@/hooks/use-auth";
import { submitTestTransaction } from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/formatters";
import type { TestTransactionPayload, TestTransactionResult, TransactionChannel, TransactionType } from "@/types";

const TRANSACTION_TYPES: TransactionType[] = ["TRANSFER", "WITHDRAWAL", "DEPOSIT", "PAYMENT", "CARD_PAYMENT"];
const CHANNELS: TransactionChannel[] = ["MOBILE", "WEB", "ATM", "POS", "BRANCH"];

const DEFAULT_FORM: TestTransactionPayload = {
  sourceAccountId: "ACC-1001",
  destinationAccountId: "",
  amount: 100,
  currency: "USD",
  transactionType: "PAYMENT",
  channel: "WEB",
  merchantCategory: "",
  country: "US",
  deviceId: "",
  ipAddress: "",
};

interface Template {
  label: string;
  description: string;
  payload: Partial<TestTransactionPayload>;
}

const TEMPLATES: Template[] = [
  {
    label: "Normal Payment",
    description: "Small amount, familiar country, business hours",
    payload: { amount: 75, currency: "USD", transactionType: "PAYMENT", channel: "WEB", country: "US" },
  },
  {
    label: "Suspicious Crypto",
    description: "Large amount to a high-risk crypto merchant",
    payload: {
      amount: 8500,
      currency: "USD",
      transactionType: "TRANSFER",
      channel: "WEB",
      merchantCategory: "CRYPTO",
      country: "US",
    },
  },
  {
    label: "High Frequency",
    description: "Rapid small transfers from a new device",
    payload: {
      amount: 250,
      currency: "USD",
      transactionType: "TRANSFER",
      channel: "MOBILE",
      country: "US",
      deviceId: `DEVICE-TEST-${Date.now()}`,
    },
  },
  {
    label: "Critical Fraud Simulation",
    description: "Large amount, new country, gambling merchant, unusual hour",
    payload: {
      amount: 15000,
      currency: "USD",
      transactionType: "TRANSFER",
      channel: "WEB",
      merchantCategory: "GAMBLING",
      country: "NG",
      deviceId: `DEVICE-TEST-${Date.now()}`,
    },
  },
];

export default function TestTransactionsPage() {
  const { hasRole } = useAuth();
  const [form, setForm] = useState<TestTransactionPayload>(DEFAULT_FORM);
  const [result, setResult] = useState<TestTransactionResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!hasRole("ADMIN", "ANALYST", "TESTER")) {
    return (
      <div className="space-y-6">
        <PageHeader title="Test Transactions" description="Manually exercise the fraud detection pipeline" />
        <AccessDenied message="Only Admins, Analysts, and Testers can submit test transactions." />
      </div>
    );
  }

  function applyTemplate(template: Template) {
    setForm({ ...DEFAULT_FORM, ...template.payload });
    setResult(null);
    setError(null);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    setIsSubmitting(true);
    try {
      const payload: TestTransactionPayload = {
        ...form,
        destinationAccountId: form.destinationAccountId || undefined,
        merchantCategory: form.merchantCategory || undefined,
        deviceId: form.deviceId || undefined,
        ipAddress: form.ipAddress || undefined,
      };
      const response = await submitTestTransaction(payload);
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to submit test transaction");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Test Transactions"
        description="Manually submit a transaction and immediately see the risk score and fraud alert result"
      />

      <SectionCard title="Quick Templates" description="Pre-filled scenarios covering the risk spectrum">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {TEMPLATES.map((template) => (
            <button
              key={template.label}
              type="button"
              onClick={() => applyTemplate(template)}
              className="rounded-md border border-border p-3 text-left text-sm transition-colors hover:border-primary hover:bg-accent"
            >
              <p className="font-medium text-foreground">{template.label}</p>
              <p className="mt-1 text-xs text-muted-foreground">{template.description}</p>
            </button>
          ))}
        </div>
      </SectionCard>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <SectionCard title="Transaction Details">
          <form onSubmit={handleSubmit} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Source Account</label>
                <Input
                  value={form.sourceAccountId}
                  onChange={(e) => setForm({ ...form, sourceAccountId: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Destination Account</label>
                <Input
                  value={form.destinationAccountId ?? ""}
                  onChange={(e) => setForm({ ...form, destinationAccountId: e.target.value })}
                  placeholder="Optional"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Amount</label>
                <Input
                  type="number"
                  min={0}
                  step="0.01"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: Number(e.target.value) })}
                  required
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Currency</label>
                <Input value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value })} required />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Transaction Type</label>
                <Select
                  value={form.transactionType}
                  onChange={(e) => setForm({ ...form, transactionType: e.target.value as TransactionType })}
                >
                  {TRANSACTION_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Channel</label>
                <Select value={form.channel} onChange={(e) => setForm({ ...form, channel: e.target.value as TransactionChannel })}>
                  {CHANNELS.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Merchant Category</label>
                <Input
                  value={form.merchantCategory ?? ""}
                  onChange={(e) => setForm({ ...form, merchantCategory: e.target.value })}
                  placeholder="e.g. CRYPTO, GAMBLING"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Country</label>
                <Input value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} required />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">Device ID</label>
                <Input
                  value={form.deviceId ?? ""}
                  onChange={(e) => setForm({ ...form, deviceId: e.target.value })}
                  placeholder="Optional"
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">IP Address</label>
                <Input
                  value={form.ipAddress ?? ""}
                  onChange={(e) => setForm({ ...form, ipAddress: e.target.value })}
                  placeholder="Optional"
                />
              </div>
            </div>

            {error && (
              <div className="flex items-start gap-2 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-xs text-destructive">
                <AlertCircle className="mt-0.5 h-3.5 w-3.5 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <Button type="submit" className="w-full" disabled={isSubmitting}>
              <FlaskConical className="mr-2 h-4 w-4" />
              {isSubmitting ? "Submitting..." : "Submit Test Transaction"}
            </Button>
          </form>
        </SectionCard>

        <SectionCard title="Result" description="Risk score and fraud alert generated by the pipeline">
          {!result ? (
            <div className="flex flex-col items-center justify-center gap-2 py-16 text-center text-muted-foreground">
              <FlaskConical className="h-6 w-6" />
              <p className="text-sm">Submit a transaction to see the result here.</p>
            </div>
          ) : (
            <div className="space-y-4">
              <div
                className={`flex items-start gap-2 rounded-md border px-3 py-2 text-sm ${
                  result.alertGenerated
                    ? "border-destructive/30 bg-destructive/10 text-destructive"
                    : "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-400"
                }`}
              >
                {result.alertGenerated ? (
                  <ShieldAlert className="mt-0.5 h-4 w-4 shrink-0" />
                ) : (
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
                )}
                <span>{result.message}</span>
              </div>

              <dl className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <dt className="text-xs text-muted-foreground">Transaction ID</dt>
                  <dd className="font-medium text-foreground">{result.transaction.transactionId}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Status</dt>
                  <dd><TransactionStatusBadge status={result.transaction.status} /></dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Amount</dt>
                  <dd className="font-medium text-foreground">
                    {formatCurrency(result.transaction.amount, result.transaction.currency)}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Risk Level</dt>
                  <dd><RiskBadge level={result.riskScore.riskLevel} /></dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Rule Score</dt>
                  <dd className="font-medium text-foreground">{result.riskScore.ruleScore}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">ML Score</dt>
                  <dd className="font-medium text-foreground">{result.riskScore.mlScore ?? "N/A"}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Final Score</dt>
                  <dd className="font-medium text-foreground">{result.riskScore.finalScore}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted-foreground">Scoring Source</dt>
                  <dd className="font-medium text-foreground">{result.riskScore.scoringSource ?? "N/A"}</dd>
                </div>
              </dl>

              {result.riskScore.triggeredRules && (
                <div>
                  <dt className="text-xs text-muted-foreground">Triggered Rules</dt>
                  <dd className="text-sm text-foreground">{result.riskScore.triggeredRules}</dd>
                </div>
              )}

              {result.fraudAlert && (
                <div className="rounded-md border border-border p-3">
                  <p className="text-xs text-muted-foreground">Fraud Alert</p>
                  <p className="mt-1 text-sm font-medium text-foreground">{result.fraudAlert.alertId}</p>
                  <p className="mt-1 text-xs text-muted-foreground">{result.fraudAlert.message}</p>
                  <p className="mt-1 text-xs text-muted-foreground">Created {formatDate(result.fraudAlert.createdAt)}</p>
                </div>
              )}
            </div>
          )}
        </SectionCard>
      </div>
    </div>
  );
}
