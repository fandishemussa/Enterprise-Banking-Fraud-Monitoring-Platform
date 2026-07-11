import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatDate, titleCase } from "@/lib/formatters";
import type { CaseTimelineEvent } from "@/types";

export function CaseTimeline({ events }: { events: CaseTimelineEvent[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Timeline</CardTitle>
      </CardHeader>
      <CardContent>
        {events.length === 0 ? (
          <p className="text-sm text-muted-foreground">No timeline events yet.</p>
        ) : (
          <ol className="space-y-4 border-l border-border pl-4">
            {events.map((event) => (
              <li key={event.eventId} className="relative">
                <span className="absolute -left-[21px] top-1 h-2.5 w-2.5 rounded-full bg-primary" />
                <div className="flex items-center justify-between text-sm">
                  <span className="font-medium">{titleCase(event.eventType)}</span>
                  <span className="text-xs text-muted-foreground">{formatDate(event.createdAt)}</span>
                </div>
                <p className="text-sm text-muted-foreground">{event.title}</p>
                {event.description && <p className="text-xs text-muted-foreground">{event.description}</p>}
                {event.actorUsername && (
                  <p className="text-xs text-muted-foreground">
                    by {event.actorUsername} {event.actorRole ? `(${event.actorRole})` : ""}
                  </p>
                )}
              </li>
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  );
}
