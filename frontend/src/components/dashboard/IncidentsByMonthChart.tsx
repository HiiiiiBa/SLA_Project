"use client";

import {
  Bar,
  BarChart,
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
        Incidents :{" "}
        <span className="font-semibold text-heading">{payload[0]?.value}</span>
      </div>
    </div>
  );
}

export function IncidentsByMonthChart({ data }: { data: TimePoint[] }) {
  if (data.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-muted">
        Aucun historique d&apos;incidents
      </div>
    );
  }

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} barSize={36}>
          <CartesianGrid
            strokeDasharray="3 8"
            vertical={false}
            stroke="currentColor"
            className="text-border/70"
          />
          <XAxis
            dataKey="label"
            tick={{ fill: "currentColor", fontSize: 12 }}
            className="text-muted"
            axisLine={false}
            tickLine={false}
          />
          <YAxis
            allowDecimals={false}
            tick={{ fill: "currentColor", fontSize: 12 }}
            className="text-muted"
            axisLine={false}
            tickLine={false}
            width={36}
          />
          <Tooltip cursor={{ fill: "rgba(148, 163, 184, 0.08)" }} content={<ChartTooltip />} />
          <Bar dataKey="value" fill="#818cf8" radius={[10, 10, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
