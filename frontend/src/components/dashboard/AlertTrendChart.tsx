"use client";

import {
  Area,
  AreaChart,
  CartesianGrid,
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
        Alertes :{" "}
        <span className="font-semibold text-heading">{payload[0]?.value}</span>
      </div>
    </div>
  );
}

export function AlertTrendChart({ data }: { data: TimePoint[] }) {
  if (data.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-muted">
        Aucune alerte sur la période
      </div>
    );
  }

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={data} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="alertTrendFill" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#f59e0b" stopOpacity={0.35} />
              <stop offset="100%" stopColor="#f59e0b" stopOpacity={0.02} />
            </linearGradient>
          </defs>
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
            allowDecimals={false}
            tick={{ fill: "currentColor", fontSize: 12 }}
            className="text-muted"
            axisLine={false}
            tickLine={false}
            width={36}
          />
          <Tooltip content={<ChartTooltip />} />
          <Area
            type="monotone"
            dataKey="value"
            stroke="#f59e0b"
            strokeWidth={2.5}
            fill="url(#alertTrendFill)"
            activeDot={{ r: 5, fill: "#f59e0b" }}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
