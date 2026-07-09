"use client";

import { getRiskScores, getTransactions } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useTransactions() {
  return useApiResource(getTransactions);
}

export function useRiskScores() {
  return useApiResource(getRiskScores);
}
