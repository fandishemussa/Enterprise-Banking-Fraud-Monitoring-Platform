"use client";

import { useCallback, useEffect, useState } from "react";
import {
  getStreamingDeadLetterEvents,
  getStreamingEvents,
  getStreamingMetrics,
  ignoreStreamingDeadLetterEvent,
  publishStreamingTransaction,
  retryStreamingDeadLetterEvent,
} from "@/lib/api";
import type { DeadLetterEvent, PublishStreamingTransactionPayload, StreamingEventLog, StreamingMetric } from "@/types";

export function useStreamingMonitor() {
  const [metric, setMetric] = useState<StreamingMetric | null>(null);
  const [events, setEvents] = useState<StreamingEventLog[]>([]);
  const [deadLetterEvents, setDeadLetterEvents] = useState<DeadLetterEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingMockData, setUsingMockData] = useState(false);

  const load = useCallback(() => {
    setIsLoading(true);
    setError(null);
    Promise.all([getStreamingMetrics(), getStreamingEvents(), getStreamingDeadLetterEvents()])
      .then(([metricResult, eventsResult, deadLetterResult]) => {
        setMetric(metricResult.data);
        setEvents(eventsResult.data);
        setDeadLetterEvents(deadLetterResult.data);
        setUsingMockData(metricResult.usingMockData || eventsResult.usingMockData || deadLetterResult.usingMockData);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load streaming data"))
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => load(), [load]);

  async function publish(payload: PublishStreamingTransactionPayload) {
    const eventId = await publishStreamingTransaction(payload);
    load();
    return eventId;
  }

  async function retry(eventId: string) {
    await retryStreamingDeadLetterEvent(eventId);
    load();
  }

  async function ignore(eventId: string) {
    await ignoreStreamingDeadLetterEvent(eventId);
    load();
  }

  return { metric, events, deadLetterEvents, isLoading, error, usingMockData, refetch: load, publish, retry, ignore };
}
