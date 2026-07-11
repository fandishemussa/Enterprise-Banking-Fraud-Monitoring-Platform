"use client";

import { useState } from "react";
import { bulkUpdateCases, createCase, getCases, updateCase } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useCases() {
  const resource = useApiResource(getCases);
  const [mutating, setMutating] = useState(false);

  async function create(payload: { alertId: string; assignedTo?: string; notes?: string }) {
    setMutating(true);
    try {
      const result = await createCase(payload);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  async function update(caseId: string, payload: { status?: string; assignedTo?: string; notes?: string }) {
    setMutating(true);
    try {
      const result = await updateCase(caseId, payload);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  async function bulkUpdate(caseIds: string[], payload: { status?: string; assignedTo?: string }) {
    setMutating(true);
    try {
      const result = await bulkUpdateCases(caseIds, payload);
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  return { ...resource, mutating, create, update, bulkUpdate };
}
