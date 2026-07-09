import { CheckCircle2, Clock, XCircle } from "lucide-react";
import { formatDurationMs, formatPercent, formatRelativeTime } from "@/lib/formatters";
import type { PipelineMetrics } from "@/types";

export function PipelineHealthSummary({ metrics }: { metrics: PipelineMetrics }) {
  const items = [
    {
      label: "Success rate",
      value: formatPercent(metrics.successRatePercent),
      icon: CheckCircle2,
      tone: "text-emerald-600 dark:text-emerald-400",
    },
    {
      label: "Avg duration",
      value: formatDurationMs(metrics.averageDurationMs),
      icon: Clock,
      tone: "text-blue-600 dark:text-blue-400",
    },
    {
      label: "Failed runs",
      value: String(metrics.failedRuns),
      icon: XCircle,
      tone: "text-red-600 dark:text-red-400",
    },
  ];

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-3 gap-3">
        {items.map((item) => (
          <div key={item.label} className="rounded-lg border border-border p-3">
            <item.icon className={`h-4 w-4 ${item.tone}`} />
            <p className="mt-2 text-lg font-semibold text-foreground">{item.value}</p>
            <p className="text-xs text-muted-foreground">{item.label}</p>
          </div>
        ))}
      </div>
      <p className="text-xs text-muted-foreground">
        Last successful run: {formatRelativeTime(metrics.lastSuccessfulRunAt)}
        {metrics.lastFailureReason && (
          <>
            {" "}
            &middot; Last failure: <span className="text-destructive">{metrics.lastFailureReason}</span>
          </>
        )}
      </p>
    </div>
  );
}
