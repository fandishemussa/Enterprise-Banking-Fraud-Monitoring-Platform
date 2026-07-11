"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { getNotifications } from "@/lib/api";
import { useAuth } from "@/hooks/use-auth";
import type { NotificationItem } from "@/types";

const POLL_INTERVAL_MS = 30_000;

export function useNotifications() {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [readIds, setReadIds] = useState<Set<string>>(new Set());
  const isFirstLoad = useRef(true);

  const poll = useCallback(() => {
    getNotifications()
      .then((items) => {
        setNotifications(items);
        // The first load's items are this user's last-24h history, not "new" - start them all
        // read so the bell doesn't show a misleadingly large count right after login. Only items
        // that show up in a later poll (i.e. genuinely new since login) count as unread.
        if (isFirstLoad.current) {
          isFirstLoad.current = false;
          setReadIds(new Set(items.map((n) => n.id)));
        }
      })
      .catch(() => {
        /* transient failures are silently ignored - the bell just shows stale data until the next poll */
      });
  }, []);

  useEffect(() => {
    if (!isAuthenticated) return;
    poll();
    const interval = setInterval(poll, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [isAuthenticated, poll]);

  const unreadCount = notifications.filter((n) => !readIds.has(n.id)).length;

  function markAllRead() {
    setReadIds(new Set(notifications.map((n) => n.id)));
  }

  return { notifications, unreadCount, markAllRead, refetch: poll };
}
