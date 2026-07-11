"use client";

import { getLinkedCustomers } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useLinkedCustomers(customerId: string) {
  return useApiResource(() => getLinkedCustomers(customerId), [customerId]);
}
