"use client";

import { getAccounts, getCustomers } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useCustomers() {
  return useApiResource(getCustomers);
}

export function useAccounts() {
  return useApiResource(getAccounts);
}
