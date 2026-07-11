import { Send, Inbox, XCircle, ShieldAlert } from "lucide-react";
import { MetricCard } from "@/components/dashboard/metric-card";
import { formatDate, formatDurationMs } from "@/lib/formatters";
import type { StreamingMetric } from "@/types";

export function StreamingSummaryCards({ metric }: { metric: StreamingMetric }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard label="Events Produced" value={String(metric.eventsProduced)} icon={Send} tone="info" />
      <MetricCard
        label="Events Consumed"
        value={String(metric.eventsConsumed)}
        hint={`Avg latency ${formatDurationMs(metric.averageProcessingLatencyMs)}`}
        icon={Inbox}
        tone="success"
      />
      <MetricCard
        label="Failed Events"
        value={String(metric.eventsFailed)}
        icon={XCircle}
        tone={metric.eventsFailed > 0 ? "destructive" : "neutral"}
      />
      <MetricCard
        label="Alerts Generated"
        value={String(metric.alertsGenerated)}
        hint={metric.lastEventAt ? `Last event ${formatDate(metric.lastEventAt)}` : "No events yet"}
        icon={ShieldAlert}
        tone={metric.alertsGenerated > 0 ? "warning" : "neutral"}
      />
    </div>
  );
}
