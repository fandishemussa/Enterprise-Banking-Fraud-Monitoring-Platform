"use client";

import { getAnalyticsSummary } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useAnalytics() {
  return useApiResource(getAnalyticsSummary);
}
