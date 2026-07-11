"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  ArrowLeftRight,
  Gauge,
  ShieldAlert,
  FolderSearch,
  Users,
  Wallet,
  Workflow,
  CheckCircle2,
  Inbox,
  ScrollText,
  Cpu,
  Settings,
  ShieldCheck,
  FlaskConical,
  UserCog,
  ListChecks,
  Timer,
  Radio,
  Activity,
  Lock,
  BarChart3,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuth } from "@/hooks/use-auth";
import type { Role } from "@/types";

interface NavItem {
  label: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  roles?: Role[];
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

const NAV_GROUPS: NavGroup[] = [
  {
    label: "Command Center",
    items: [{ label: "Dashboard", href: "/dashboard", icon: LayoutDashboard }],
  },
  {
    label: "Monitoring",
    items: [
      { label: "Transactions", href: "/transactions", icon: ArrowLeftRight },
      { label: "Risk Scores", href: "/risk-scores", icon: Gauge },
      { label: "Risk Rules", href: "/risk-rules", icon: ListChecks },
      { label: "Fraud Alerts", href: "/alerts", icon: ShieldAlert },
      { label: "Investigation Cases", href: "/cases", icon: FolderSearch },
      {
        label: "SLA Monitoring",
        href: "/sla-monitoring",
        icon: Timer,
        roles: ["ADMIN", "ANALYST", "INVESTIGATOR", "VIEWER"],
      },
    ],
  },
  {
    label: "Customers",
    items: [
      { label: "Customers", href: "/customers", icon: Users },
      { label: "Accounts", href: "/accounts", icon: Wallet },
    ],
  },
  {
    label: "Data Engineering",
    items: [
      { label: "Pipeline Observability", href: "/pipeline-observability", icon: Workflow },
      { label: "Data Quality", href: "/data-quality", icon: CheckCircle2 },
      { label: "Dead Letter Queue", href: "/dead-letter", icon: Inbox },
      {
        label: "Streaming Monitor",
        href: "/streaming-monitor",
        icon: Radio,
        roles: ["ADMIN", "ANALYST", "TESTER"],
      },
    ],
  },
  {
    label: "Governance",
    items: [
      { label: "Audit Logs", href: "/audit-logs", icon: ScrollText },
      {
        label: "Customer Locks",
        href: "/customer-locks",
        icon: Lock,
        roles: ["ADMIN", "INVESTIGATOR"],
      },
      {
        label: "Analytics",
        href: "/analytics",
        icon: BarChart3,
        roles: ["ADMIN", "VIEWER"],
      },
    ],
  },
  {
    label: "ML Operations",
    items: [
      { label: "ML Service", href: "/ml-service", icon: Cpu },
      {
        label: "Model Monitoring",
        href: "/model-monitoring",
        icon: Activity,
        roles: ["ADMIN", "ANALYST", "VIEWER"],
      },
    ],
  },
  {
    label: "Testing",
    items: [
      {
        label: "Test Transactions",
        href: "/test-transactions",
        icon: FlaskConical,
        roles: ["ADMIN", "ANALYST", "TESTER"],
      },
    ],
  },
  {
    label: "Administration",
    items: [
      { label: "Admin Settings", href: "/admin", icon: Settings },
      { label: "Users", href: "/users", icon: UserCog, roles: ["ADMIN"] },
    ],
  },
];

export function Sidebar({ className }: { className?: string }) {
  const pathname = usePathname();
  const { user, hasRole } = useAuth();

  return (
    <aside className={cn("flex h-full w-64 flex-col border-r border-border bg-card", className)}>
      <div className="flex h-14 items-center gap-2 border-b border-border px-5">
        <ShieldCheck className="h-5 w-5 text-primary" />
        <span className="text-sm font-semibold tracking-tight text-foreground">Fraud Monitoring</span>
      </div>
      <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-4">
        {NAV_GROUPS.map((group) => {
          const visibleItems = group.items.filter((item) => !item.roles || (user && hasRole(...item.roles)));
          if (visibleItems.length === 0) return null;

          return (
          <div key={group.label}>
            <p className="px-2 pb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {group.label}
            </p>
            <div className="space-y-0.5">
              {visibleItems.map((item) => {
                const active = pathname === item.href || pathname?.startsWith(item.href + "/");
                const Icon = item.icon;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={cn(
                      "flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm font-medium transition-colors",
                      active
                        ? "bg-primary/10 text-primary"
                        : "text-muted-foreground hover:bg-accent hover:text-foreground",
                    )}
                  >
                    <Icon className="h-4 w-4 shrink-0" />
                    {item.label}
                  </Link>
                );
              })}
            </div>
          </div>
          );
        })}
      </nav>
      <div className="border-t border-border px-5 py-3 text-xs text-muted-foreground">
        Enterprise Banking Fraud Monitoring &copy; 2026
      </div>
    </aside>
  );
}
