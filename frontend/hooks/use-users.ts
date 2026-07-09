"use client";

import { useCallback, useEffect, useState } from "react";
import { getUsers } from "@/lib/api";
import type { AppUser } from "@/types";

export function useUsers() {
  const [data, setData] = useState<AppUser[] | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState(0);

  const load = useCallback(() => {
    let cancelled = false;
    setIsLoading(true);
    setError(null);

    getUsers()
      .then((users) => {
        if (!cancelled) setData(users);
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : "Failed to load users");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [reloadToken]);

  useEffect(() => load(), [load]);

  const refetch = useCallback(() => setReloadToken((token) => token + 1), []);

  return { data, isLoading, error, refetch };
}
