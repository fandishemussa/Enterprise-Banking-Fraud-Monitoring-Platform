"use client";

import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

export function FraudTrendChart({
  data,
}: {
  data: { date: string; alerts: number; transactions: number }[];
}) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <LineChart data={data} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
        <XAxis dataKey="date" tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
        <YAxis tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
        <Tooltip
          contentStyle={{ borderRadius: 8, border: "1px solid hsl(var(--border))", fontSize: 12 }}
        />
        <Line type="monotone" dataKey="transactions" stroke="#3b82f6" strokeWidth={2} dot={false} name="Transactions" />
        <Line type="monotone" dataKey="alerts" stroke="#ef4444" strokeWidth={2} dot={false} name="Fraud Alerts" />
      </LineChart>
    </ResponsiveContainer>
  );
}
