"use client";

import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import type { Alert } from "@/types";

const statusLabels: Record<string, string> = {
  NEW: "Nouvelles",
  READ: "Lues",
  RESOLVED: "Résolues",
};

const statusColors: Record<string, string> = {
  NEW: "#f59e0b",
  READ: "#3b82f6",
  RESOLVED: "#10b981",
};

const statusOrder = ["NEW", "READ", "RESOLVED"];

function ChartTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: Array<{ name?: string; value?: number; payload?: { fill?: string } }>;
}) {
  if (!active || !payload?.length) return null;
  const item = payload[0];
  const value = item?.value ?? 0;
  const color = item?.payload?.fill ?? "#94a3b8";
  return (
    <div
      className="rounded-xl border border-border bg-card/95 px-4 py-3 text-sm shadow-xl backdrop-blur"
      style={{ boxShadow: "0 20px 50px rgba(2, 6, 23, 0.25)" }}
    >
      <div className="flex items-center justify-between gap-3">
        <span className="inline-flex items-center gap-2 font-semibold text-heading">
          <span className="h-2.5 w-2.5 rounded-full" style={{ background: color }} />
          {item?.name}
        </span>
        <span className="font-semibold text-heading">{value}</span>
      </div>
      <div className="mt-1 text-xs text-muted">Nombre d&apos;alertes</div>
    </div>
  );
}

export function AlertStatusChart({ alerts }: { alerts: Alert[] }) {
  const counts = alerts.reduce<Record<string, number>>((acc, alert) => {
    acc[alert.status] = (acc[alert.status] ?? 0) + 1;
    return acc;
  }, {});

  const data = statusOrder
    .filter((status) => (counts[status] ?? 0) > 0)
    .map((status) => ({
      name: statusLabels[status] ?? status,
      value: counts[status] ?? 0,
      fill: statusColors[status] ?? "#64748b",
    }));

  if (data.length === 0) {
    return (
      <div className="flex h-56 items-center justify-center text-sm text-muted">
        Aucune alerte enregistrée
      </div>
    );
  }

  return (
    <div className="h-56 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="50%"
            innerRadius={52}
            outerRadius={78}
            paddingAngle={3}
          >
            {data.map((entry) => (
              <Cell key={entry.name} fill={entry.fill} stroke="transparent" />
            ))}
          </Pie>
          <Tooltip content={<ChartTooltip />} />
          <Legend
            verticalAlign="bottom"
            height={36}
            formatter={(value) => <span className="text-xs text-muted">{value}</span>}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
