"use client";

import { useCallback, useEffect, useState } from "react";
import type { ApiResult } from "@/types";

interface UseApiResourceState<T> {
  data: T | null;
  isLoading: boolean;
  error: string | null;
  usingMockData: boolean;
  refetch: () => void;
}

/** Shared data-fetching hook: calls the given fetcher (which already falls back to mock data
 * internally in lib/api.ts) and exposes loading/usingMockData state for the UI to react to. */
export function useApiResource<T>(fetcher: () => Promise<ApiResult<T>>, deps: unknown[] = []): UseApiResourceState<T> {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingMockData, setUsingMockData] = useState(false);
  const [reloadToken, setReloadToken] = useState(0);

  const load = useCallback(() => {
    let cancelled = false;
    setIsLoading(true);
    setError(null);

    fetcher()
      .then((result) => {
        if (cancelled) return;
        setData(result.data);
        setUsingMockData(result.usingMockData);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof Error ? err.message : "Failed to load data");
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reloadToken, ...deps]);

  useEffect(() => load(), [load]);

  const refetch = useCallback(() => setReloadToken((token) => token + 1), []);

  return { data, isLoading, error, usingMockData, refetch };
}
