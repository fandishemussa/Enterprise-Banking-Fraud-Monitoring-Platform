"use client";

import { PageHeader } from "@/components/layout/page-header";
import { DemoDataBanner } from "@/components/shared/demo-data-banner";
import { LoadingState } from "@/components/shared/loading-state";
import { ErrorState } from "@/components/shared/error-state";
import { StreamingSummaryCards } from "@/components/streaming/streaming-summary-cards";
import { StreamingMetricsChart } from "@/components/streaming/streaming-metrics-chart";
import { StreamingEventsTable } from "@/components/streaming/streaming-events-table";
import { StreamingDeadLetterTable } from "@/components/streaming/streaming-dead-letter-table";
import { PublishStreamingTransactionForm } from "@/components/streaming/publish-streaming-transaction-form";
import { useStreamingMonitor } from "@/hooks/use-streaming-monitor";

export default function StreamingMonitorPage() {
  const { metric, events, deadLetterEvents, isLoading, error, usingMockData, refetch, publish, retry, ignore } =
    useStreamingMonitor();

  return (
    <div className="space-y-6">
      <PageHeader title="Streaming Monitor" description="Real-time transaction ingestion via Kafka-compatible Redpanda" />
      {usingMockData && <DemoDataBanner />}

      {isLoading ? (
        <LoadingState label="Loading streaming data..." />
      ) : error || !metric ? (
        <ErrorState message={error ?? "Failed to load streaming metrics"} onRetry={refetch} />
      ) : (
        <div className="space-y-6">
          <StreamingSummaryCards metric={metric} />
          <div className="grid gap-6 lg:grid-cols-2">
            <StreamingMetricsChart metric={metric} />
            <PublishStreamingTransactionForm onPublish={publish} />
          </div>
          <StreamingEventsTable events={events} />
          <StreamingDeadLetterTable events={deadLetterEvents} onRetry={retry} onIgnore={ignore} />
        </div>
      )}
    </div>
  );
}
