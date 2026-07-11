"use client";

import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { CustomerRiskTrendPoint } from "@/types";

export function CustomerRiskTrendChart({ data }: { data: CustomerRiskTrendPoint[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Risk Score Trend</CardTitle>
      </CardHeader>
      <CardContent>
        {data.length === 0 ? (
          <p className="text-sm text-muted-foreground">No risk score history yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={data} margin={{ top: 8, right: 12, left: -12, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-border" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
              <YAxis tick={{ fontSize: 12 }} stroke="currentColor" className="text-muted-foreground" />
              <Tooltip contentStyle={{ borderRadius: 8, border: "1px solid hsl(var(--border))", fontSize: 12 }} />
              <Line type="monotone" dataKey="averageRiskScore" stroke="#ef4444" strokeWidth={2} dot={false} name="Avg Risk Score" />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}
