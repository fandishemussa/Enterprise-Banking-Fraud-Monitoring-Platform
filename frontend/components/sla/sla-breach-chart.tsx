"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { SlaSummary } from "@/types";

export function SlaBreachChart({ summary }: { summary: SlaSummary }) {
  const data = [
    { name: "On Track", count: summary.onTrack },
    { name: "Near Breach", count: summary.nearBreach },
    { name: "Breached", count: summary.breached },
    { name: "Completed", count: summary.completed },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>SLA Status Distribution</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={data} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
            <XAxis dataKey="name" tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
            <YAxis tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
            <Tooltip contentStyle={{ borderRadius: 8, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
            <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
