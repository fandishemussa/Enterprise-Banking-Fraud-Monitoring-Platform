"use client";

import { CheckCircle2, Cpu, Server, XCircle } from "lucide-react";
import { PageHeader } from "@/components/layout/page-header";
import { SectionCard } from "@/components/shared/section-card";
import { MetricCard } from "@/components/dashboard/metric-card";
import { Badge } from "@/components/ui/badge";
import { API_BASE_URL, ML_SERVICE_URL } from "@/lib/endpoints";
import { useMlHealth } from "@/hooks/use-ml-service";
import { useCustomers } from "@/hooks/use-customers";

const RISK_RULES = [
  { name: "LargeAmountRule", detail: "Amount > 5,000 (+30) / > 10,000 (+50)" },
  { name: "NewDeviceRule", detail: "First transaction from this device (+20)" },
  { name: "NewCountryRule", detail: "First transaction from this country (+25)" },
  { name: "HighFrequencyTransactionRule", detail: "> 5 transactions in last 10 minutes (+35)" },
  { name: "HighRiskMerchantRule", detail: "CRYPTO / GAMBLING / HIGH_RISK_TRANSFER (+25)" },
  { name: "UnusualHourRule", detail: "Transaction between 00:00-05:00 (+15)" },
];

export default function AdminPage() {
  const { usingMockData: backendUsingMock } = useCustomers();
  const { data: mlHealth, usingMockData: mlUsingMock } = useMlHealth();

  const backendOnline = !backendUsingMock;
  const mlOnline = !mlUsingMock && mlHealth?.status === "healthy";

  return (
    <div className="space-y-6">
      <PageHeader title="Admin Settings" description="Platform configuration and system status" />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <MetricCard label="Backend API" value={backendOnline ? "Connected" : "Unavailable"} icon={Server} tone={backendOnline ? "success" : "destructive"} />
        <MetricCard label="ML Service" value={mlOnline ? "Connected" : "Unavailable"} icon={Cpu} tone={mlOnline ? "success" : "destructive"} />
        <MetricCard label="App Version" value="1.0.0" icon={CheckCircle2} tone="neutral" />
      </div>

      <SectionCard title="Connection Settings" description="Environment variables read at build time">
        <dl className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-xs text-muted-foreground">NEXT_PUBLIC_API_BASE_URL</dt>
            <dd className="text-sm font-medium text-foreground">{API_BASE_URL}</dd>
          </div>
          <div>
            <dt className="text-xs text-muted-foreground">NEXT_PUBLIC_ML_SERVICE_URL</dt>
            <dd className="text-sm font-medium text-foreground">{ML_SERVICE_URL}</dd>
          </div>
        </dl>
      </SectionCard>

      <SectionCard title="Rule-Based Risk Scoring Summary" description="Six pluggable rules, capped at 100, mapped to LOW/MEDIUM/HIGH/CRITICAL">
        <ul className="divide-y divide-border">
          {RISK_RULES.map((rule) => (
            <li key={rule.name} className="flex items-center justify-between gap-4 py-2.5">
              <span className="text-sm font-medium text-foreground">{rule.name}</span>
              <span className="text-xs text-muted-foreground">{rule.detail}</span>
            </li>
          ))}
        </ul>
      </SectionCard>

      <SectionCard title="System Status">
        <div className="flex flex-wrap gap-2">
          <Badge variant={backendOnline ? "success" : "destructive"}>
            {backendOnline ? <CheckCircle2 className="mr-1 h-3 w-3" /> : <XCircle className="mr-1 h-3 w-3" />}
            Spring Boot Backend
          </Badge>
          <Badge variant={mlOnline ? "success" : "destructive"}>
            {mlOnline ? <CheckCircle2 className="mr-1 h-3 w-3" /> : <XCircle className="mr-1 h-3 w-3" />}
            FastAPI ML Service
          </Badge>
        </div>
      </SectionCard>
    </div>
  );
}
