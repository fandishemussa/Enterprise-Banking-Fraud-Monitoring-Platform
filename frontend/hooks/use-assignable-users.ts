"use client";

import { useEffect, useState } from "react";
import { getAssignableUsers } from "@/lib/api";
import type { AssignableUser } from "@/types";

export function useAssignableUsers() {
  const [data, setData] = useState<AssignableUser[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    getAssignableUsers()
      .then((users) => {
        if (!cancelled) setData(users);
      })
      .catch(() => {
        if (!cancelled) setData([]);
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { data, isLoading };
}
