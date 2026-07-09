import type { LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

export function MetricCard({
  label,
  value,
  icon: Icon,
  hint,
  tone = "neutral",
}: {
  label: string;
  value: string;
  icon: LucideIcon;
  hint?: string;
  tone?: "neutral" | "success" | "warning" | "destructive" | "info";
}) {
  const toneClasses: Record<string, string> = {
    neutral: "bg-slate-100 text-slate-700 dark:bg-slate-500/15 dark:text-slate-300",
    success: "bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-400",
    warning: "bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-400",
    destructive: "bg-red-100 text-red-700 dark:bg-red-500/15 dark:text-red-400",
    info: "bg-blue-100 text-blue-700 dark:bg-blue-500/15 dark:text-blue-400",
  };

  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-3 p-5">
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
          <p className="mt-1.5 text-2xl font-semibold tracking-tight text-foreground">{value}</p>
          {hint && <p className="mt-1 truncate text-xs text-muted-foreground">{hint}</p>}
        </div>
        <div className={cn("flex h-9 w-9 shrink-0 items-center justify-center rounded-lg", toneClasses[tone])}>
          <Icon className="h-4.5 w-4.5" />
        </div>
      </CardContent>
    </Card>
  );
}
