"use client";

import { useCallback, useEffect, useState } from "react";
import { getSlaAlerts, getSlaPolicies, getSlaSummary, updateSlaPolicy } from "@/lib/api";
import type { AlertSlaPolicyPayload, AlertSlaResult, SlaSummary } from "@/types";

export function useSlaMonitoring() {
  const [summary, setSummary] = useState<SlaSummary | null>(null);
  const [results, setResults] = useState<AlertSlaResult[]>([]);
  const [policies, setPolicies] = useState<
    Awaited<ReturnType<typeof getSlaPolicies>>["data"]
  >([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingMockData, setUsingMockData] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    Promise.all([getSlaSummary(), getSlaAlerts(), getSlaPolicies()])
      .then(([summaryResult, alertsResult, policiesResult]) => {
        setSummary(summaryResult.data);
        setResults(alertsResult.data);
        setPolicies(policiesResult.data);
        setUsingMockData(summaryResult.usingMockData || alertsResult.usingMockData || policiesResult.usingMockData);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load SLA data"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => load(), [load]);

  async function updatePolicy(policyId: string, payload: AlertSlaPolicyPayload) {
    await updateSlaPolicy(policyId, payload);
    load();
  }

  return { summary, results, policies, isLoading, error, usingMockData, refetch: load, updatePolicy };
}
