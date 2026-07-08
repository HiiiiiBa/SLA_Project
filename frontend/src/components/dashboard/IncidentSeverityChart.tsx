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
import type { Incident } from "@/types";

const severityLabels: Record<string, string> = {
  LOW: "Faible",
  MEDIUM: "Moyen",
  HIGH: "Élevé",
  CRITICAL: "Critique",
};

const severityColors: Record<string, string> = {
  LOW: "#64748b",
  MEDIUM: "#3b82f6",
  HIGH: "#f59e0b",
  CRITICAL: "#ef4444",
};

const severityOrder = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

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
      <div className="mt-1 text-xs text-muted">Nombre d&apos;incidents</div>
    </div>
  );
}

export function IncidentSeverityChart({ incidents }: { incidents: Incident[] }) {
  const counts = incidents.reduce<Record<string, number>>((acc, incident) => {
    acc[incident.severity] = (acc[incident.severity] ?? 0) + 1;
    return acc;
  }, {});

  const data = severityOrder
    .filter((severity) => (counts[severity] ?? 0) > 0)
    .map((severity) => ({
      severity: severityLabels[severity] ?? severity,
      count: counts[severity] ?? 0,
      fill: severityColors[severity] ?? "#64748b",
    }));

  if (data.length === 0) {
    return (
      <div className="flex h-56 items-center justify-center text-sm text-muted">
        Aucun incident enregistré
      </div>
    );
  }

  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} barSize={40}>
          <CartesianGrid
            strokeDasharray="3 8"
            vertical={false}
            stroke="currentColor"
            className="text-border/70"
          />
          <XAxis
            dataKey="severity"
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
