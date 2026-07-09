"use client";

import { useState } from "react";
import { assignAlert, getAlerts, updateAlertStatus } from "@/lib/api";
import { useApiResource } from "@/hooks/use-api-resource";

export function useAlerts() {
  const resource = useApiResource(getAlerts);
  const [mutating, setMutating] = useState(false);

  async function changeStatus(alertId: string, status: string) {
    setMutating(true);
    try {
      const result = await updateAlertStatus(alertId, { status });
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  async function assign(alertId: string, assignedTo: string) {
    setMutating(true);
    try {
      const result = await assignAlert(alertId, { assignedTo });
      resource.refetch();
      return result;
    } finally {
      setMutating(false);
    }
  }

  return { ...resource, mutating, changeStatus, assign };
}
