"use client";

import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import type { NamedCount } from "@/lib/dashboard-metrics";

function ChartTooltip({
  active,
  payload,
}: {
  active?: boolean;
  payload?: Array<{ name?: string; value?: number; payload?: { fill?: string } }>;
}) {
  if (!active || !payload?.length) return null;
  const item = payload[0];
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
        <span className="font-semibold text-heading">{item?.value}</span>
      </div>
      <div className="mt-1 text-xs text-muted">Services</div>
    </div>
  );
}

export function ServiceHealthChart({ data }: { data: NamedCount[] }) {
  if (data.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-muted">
        Aucun service monitoré
      </div>
    );
  }

  return (
    <div className="h-64 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="48%"
            innerRadius={58}
            outerRadius={86}
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
