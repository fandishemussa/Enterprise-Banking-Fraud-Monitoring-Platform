"use client";

import { getCustomerRiskProfile } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useCustomerRiskProfile(customerId: string) {
  return useApiResource(() => getCustomerRiskProfile(customerId), [customerId]);
}
