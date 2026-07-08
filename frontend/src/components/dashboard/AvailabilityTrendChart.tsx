"use client";

import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { TimePoint } from "@/lib/dashboard-metrics";

function ChartTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ value?: number }>;
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  return (
    <div
      className="rounded-xl border border-border bg-card/95 px-4 py-3 text-sm shadow-xl backdrop-blur"
      style={{ boxShadow: "0 20px 50px rgba(2, 6, 23, 0.25)" }}
    >
      <div className="font-semibold text-heading">{label}</div>
      <div className="mt-1 text-xs text-muted">
        Disponibilité :{" "}
        <span className="font-semibold text-heading">{payload[0]?.value}%</span>
      </div>
    </div>
  );
}

export function AvailabilityTrendChart({ data }: { data: TimePoint[] }) {
  if (data.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-muted">
        Aucune donnée de disponibilité
      </div>
    );
  }

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
          <CartesianGrid
            strokeDasharray="3 8"
            vertical={false}
            stroke="currentColor"
            className="text-border/70"
          />
          <XAxis
            dataKey="label"
            tick={{ fill: "currentColor", fontSize: 11 }}
            className="text-muted"
            axisLine={false}
            tickLine={false}
            interval="preserveStartEnd"
            minTickGap={28}
          />
          <YAxis
            domain={[90, 100]}
            tick={{ fill: "currentColor", fontSize: 12 }}
            className="text-muted"
            axisLine={false}
            tickLine={false}
            tickFormatter={(v) => `${v}%`}
            width={48}
          />
          <Tooltip content={<ChartTooltip />} />
          <Line
            type="monotone"
            dataKey="value"
            stroke="#38bdf8"
            strokeWidth={2.5}
            dot={false}
            activeDot={{ r: 5, fill: "#38bdf8" }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
