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
import type { Sla } from "@/types";

const statusLabels: Record<string, string> = {
  ACTIVE: "Actifs",
  INACTIVE: "Inactifs",
  WARNING: "Attention",
  BREACHED: "Violés",
  ARCHIVED: "Archivés",
};

const statusColors: Record<string, string> = {
  ACTIVE: "#10b981",
  INACTIVE: "#64748b",
  WARNING: "#f59e0b",
  BREACHED: "#ef4444",
  ARCHIVED: "#94a3b8",
};

function ChartTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ value?: number; payload?: { fill?: string } }>;
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  const value = payload[0]?.value ?? 0;
  const color = payload[0]?.payload?.fill ?? "#94a3b8";
  return (
    <div
      className="rounded-xl border border-border bg-card/95 px-4 py-3 text-sm shadow-xl backdrop-blur"
      style={{ boxShadow: "0 20px 50px rgba(2, 6, 23, 0.25)" }}
    >
      <div className="flex items-center justify-between gap-3">
        <span className="inline-flex items-center gap-2 font-semibold text-heading">
          <span className="h-2.5 w-2.5 rounded-full" style={{ background: color }} />
          {label}
        </span>
        <span className="font-semibold text-heading">{value}</span>
      </div>
      <div className="mt-1 text-xs text-muted">Nombre de contrats</div>
    </div>
  );
}

export function SlaStatusChart({ slas }: { slas: Sla[] }) {
  const counts = slas.reduce<Record<string, number>>((acc, sla) => {
    acc[sla.status] = (acc[sla.status] ?? 0) + 1;
    return acc;
  }, {});

  const data = Object.entries(counts).map(([status, count]) => ({
    status: statusLabels[status] ?? status,
    count,
    fill: statusColors[status] ?? "#64748b",
  }));

  if (data.length === 0) {
    return (
      <div className="flex h-56 items-center justify-center text-sm text-muted">
        Aucune donnée SLA disponible
      </div>
    );
  }

  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} barSize={48}>
          <CartesianGrid
            strokeDasharray="3 8"
            vertical={false}
            stroke="currentColor"
            className="text-border/70"
          />
          <XAxis
            dataKey="status"
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
          />
          <Tooltip cursor={{ fill: "rgba(148, 163, 184, 0.08)" }} content={<ChartTooltip />} />
          <Bar dataKey="count" radius={[10, 10, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
