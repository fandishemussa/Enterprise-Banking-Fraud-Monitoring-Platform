"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Bell, ShieldAlert, ArrowUpCircle, Lock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNotifications } from "@/hooks/use-notifications";
import { formatRelativeTime } from "@/lib/formatters";
import type { NotificationItem, NotificationType } from "@/types";

const ICONS: Record<NotificationType, React.ComponentType<{ className?: string }>> = {
  NEW_ALERT: ShieldAlert,
  ALERT_ESCALATED: ArrowUpCircle,
  LOCK_REQUEST_PENDING: Lock,
};

function hrefFor(item: NotificationItem): string {
  if (item.type === "LOCK_REQUEST_PENDING") return "/customer-locks";
  return "/alerts";
}

export function NotificationBell() {
  const { notifications, unreadCount, markAllRead } = useNotifications();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div ref={containerRef} className="relative">
      <Button
        variant="ghost"
        size="icon"
        aria-label="Notifications"
        onClick={() => {
          setOpen((o) => !o);
          if (!open) markAllRead();
        }}
      >
        <Bell className="h-4 w-4" />
        {unreadCount > 0 && (
          <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[9px] font-semibold text-destructive-foreground">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </Button>

      {open && (
        <div className="absolute right-0 top-full z-50 mt-2 w-80 rounded-md border border-border bg-card shadow-lg">
          <div className="border-b border-border px-3 py-2 text-xs font-semibold text-foreground">Notifications</div>
          <div className="max-h-96 overflow-y-auto">
            {notifications.length === 0 ? (
              <p className="px-3 py-6 text-center text-xs text-muted-foreground">No recent notifications.</p>
            ) : (
              notifications.map((item) => {
                const Icon = ICONS[item.type];
                return (
                  <Link
                    key={item.id}
                    href={hrefFor(item)}
                    onClick={() => setOpen(false)}
                    className="flex items-start gap-2.5 border-b border-border px-3 py-2.5 text-xs last:border-b-0 hover:bg-accent"
                  >
                    <Icon className="mt-0.5 h-3.5 w-3.5 shrink-0 text-muted-foreground" />
                    <div className="min-w-0 flex-1">
                      <p className="font-medium text-foreground">{item.title}</p>
                      <p className="truncate text-muted-foreground">{item.message}</p>
                      <p className="mt-0.5 text-[10px] text-muted-foreground">{formatRelativeTime(item.createdAt)}</p>
                    </div>
                  </Link>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}
