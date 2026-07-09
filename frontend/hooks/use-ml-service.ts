"use client";

import { getMlHealth, getMlModelInfo } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useMlHealth() {
  return useApiResource(getMlHealth);
}

export function useMlModelInfo() {
  return useApiResource(getMlModelInfo);
}
