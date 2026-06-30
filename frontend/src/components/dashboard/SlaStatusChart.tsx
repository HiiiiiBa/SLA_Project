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
  WARNING: "Attention",
  BREACHED: "Violés",
  ARCHIVED: "Archivés",
};

const statusColors: Record<string, string> = {
  ACTIVE: "#10b981",
  WARNING: "#f59e0b",
  BREACHED: "#ef4444",
  ARCHIVED: "#64748b",
};

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
      <div className="flex h-64 items-center justify-center text-sm text-slate-400">
        Aucune donnée SLA disponible
      </div>
    );
  }

  return (
    <div className="h-72 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} barSize={48}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
          <XAxis dataKey="status" tick={{ fill: "#64748b", fontSize: 12 }} />
          <YAxis allowDecimals={false} tick={{ fill: "#64748b", fontSize: 12 }} />
          <Tooltip
            cursor={{ fill: "rgba(148, 163, 184, 0.08)" }}
            contentStyle={{
              borderRadius: 12,
              borderColor: "#e2e8f0",
              boxShadow: "0 10px 30px rgba(15, 23, 42, 0.08)",
            }}
          />
          <Bar dataKey="count" radius={[10, 10, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
