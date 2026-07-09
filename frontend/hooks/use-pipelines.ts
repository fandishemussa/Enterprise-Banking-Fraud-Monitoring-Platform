"use client";

import { useState } from "react";
import {
  getAuditLogs,
  getDataQualityResults,
  getDataQualityRuns,
  getDeadLetterTransactions,
  getPipelineMetrics,
  getPipelineRuns,
  ignoreDeadLetterTransaction,
  retryDeadLetterTransaction,
} from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function usePipelineRuns() {
  return useApiResource(getPipelineRuns);
}

export function usePipelineMetrics() {
  return useApiResource(getPipelineMetrics);
}

export function useDataQualityRuns() {
  return useApiResource(getDataQualityRuns);
}

export function useDataQualityResults() {
  return useApiResource(getDataQualityResults);
}

export function useDeadLetterTransactions() {
  const resource = useApiResource(getDeadLetterTransactions);
  const [mutating, setMutating] = useState(false);

  async function retry(id: string) {
    setMutating(true);
    try {
      const result = await retryDeadLetterTransaction(id);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  async function ignore(id: string) {
    setMutating(true);
    try {
      const result = await ignoreDeadLetterTransaction(id);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  return { ...resource, mutating, retry, ignore };
}

export function useAuditLogs() {
  return useApiResource(getAuditLogs);
}
