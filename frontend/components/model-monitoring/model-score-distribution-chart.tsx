"use client";

import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { ScoreDistribution } from "@/types";

const COLORS: Record<string, string> = {
  Low: "#22c55e",
  Medium: "#3b82f6",
  High: "#f59e0b",
  Critical: "#ef4444",
};

export function ModelScoreDistributionChart({ distribution }: { distribution: ScoreDistribution }) {
  const data = [
    { name: "Low", count: distribution.low },
    { name: "Medium", count: distribution.medium },
    { name: "High", count: distribution.high },
    { name: "Critical", count: distribution.critical },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Score Distribution</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={data} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
            <XAxis dataKey="name" tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
            <YAxis tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
            <Tooltip contentStyle={{ borderRadius: 8, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
            <Bar dataKey="count" radius={[4, 4, 0, 0]}>
              {data.map((entry) => (
                <Cell key={entry.name} fill={COLORS[entry.name]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}
