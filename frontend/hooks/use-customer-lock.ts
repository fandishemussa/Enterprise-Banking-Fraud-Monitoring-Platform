"use client";

import { useCallback, useEffect, useState } from "react";
import {
  approveLockRequest,
  getCustomerLockRequests,
  lockCustomer,
  rejectLockRequest,
  unlockCustomer,
} from "@/lib/api";
import type { CustomerLockRequest } from "@/types";

export function useCustomerLock(customerId: string) {
  const [requests, setRequests] = useState<CustomerLockRequest[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const load = useCallback(() => {
    setIsLoading(true);
    getCustomerLockRequests(customerId)
      .then(setRequests)
      .catch(() => setRequests([]))
      .finally(() => setIsLoading(false));
  }, [customerId]);

  useEffect(() => load(), [load]);

  async function requestLock(reason: string) {
    const result = await lockCustomer(customerId, reason);
    load();
    return result;
  }

  async function unlock() {
    await unlockCustomer(customerId);
    load();
  }

  async function approve(lockRequestId: string, notes?: string) {
    await approveLockRequest(lockRequestId, notes);
    load();
  }

  async function reject(lockRequestId: string, notes?: string) {
    await rejectLockRequest(lockRequestId, notes);
    load();
  }

  const pendingRequest = requests.find((r) => r.status === "PENDING") ?? null;

  return { requests, pendingRequest, isLoading, refetch: load, requestLock, unlock, approve, reject };
}
