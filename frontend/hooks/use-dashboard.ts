"use client";

import { getDashboardSummary } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useDashboard() {
  return useApiResource(getDashboardSummary);
}
