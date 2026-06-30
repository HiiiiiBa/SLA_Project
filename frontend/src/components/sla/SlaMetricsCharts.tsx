"use client";

import {
  Area,
  AreaChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { MonitoringMetric } from "@/types";
import { formatDate } from "@/lib/utils";

interface SlaMetricsChartsProps {
  metrics: MonitoringMetric[];
  responseTimeLimit: number;
}

export function SlaMetricsCharts({
  metrics,
  responseTimeLimit,
}: SlaMetricsChartsProps) {
  const chartData = [...metrics]
    .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime())
    .map((metric) => ({
      label: formatDate(metric.timestamp),
      responseTime: metric.responseTime,
      errorRate: metric.errorRate,
      limit: responseTimeLimit,
      up: metric.status === "UP" ? 100 : 0,
    }));

  if (chartData.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center rounded-2xl border border-dashed border-slate-200 text-sm text-muted dark:border-slate-700">
        Aucune métrique disponible pour ce SLA.
      </div>
    );
  }

  const lastPoint = chartData[chartData.length - 1];

  function TooltipContent({
    active,
    payload,
    label,
  }: {
    active?: boolean;
    payload?: Array<{ name?: string; value?: number; stroke?: string; color?: string }>;
    label?: string;
  }) {
    if (!active || !payload?.length) return null;
    return (
      <div
        className="rounded-xl border border-border bg-card/95 px-4 py-3 text-sm shadow-xl backdrop-blur"
        style={{ boxShadow: "0 20px 50px rgba(2, 6, 23, 0.25)" }}
      >
        <div className="text-xs font-semibold text-muted">{label}</div>
        <div className="mt-2 space-y-1">
          {payload
            .filter((p) => typeof p.value === "number")
            .map((p, idx) => (
              <div key={idx} className="flex items-center justify-between gap-6">
                <span className="inline-flex items-center gap-2 text-body">
                  <span
                    className="h-2.5 w-2.5 rounded-full"
                    style={{ background: p.color ?? p.stroke ?? "#94a3b8" }}
                  />
                  {p.name}
                </span>
                <span className="font-semibold text-heading">
                  {p.name === "Erreurs" ? `${p.value?.toFixed(2)}%` : p.value?.toFixed(0)}
                </span>
              </div>
            ))}
        </div>
      </div>
    );
  }

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <div className="surface-card p-5">
        <div className="mb-4 flex items-end justify-between gap-4">
          <div>
            <h4 className="text-sm font-semibold text-heading">Temps de réponse</h4>
            <p className="mt-1 text-xs text-muted">Dernier point : {lastPoint.responseTime.toFixed(0)} ms</p>
          </div>
          <div className="text-xs text-muted">
            Limite SLA <span className="font-semibold text-heading">{responseTimeLimit} ms</span>
          </div>
        </div>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid
                strokeDasharray="3 8"
                vertical={false}
                stroke="currentColor"
                className="text-border/70"
              />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} hide />
              <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} className="text-muted" />
              <Tooltip content={<TooltipContent />} />
              <Line
                type="monotone"
                dataKey="responseTime"
                name="Réponse (ms)"
                stroke="#0ea5e9"
                strokeWidth={2}
                dot={false}
              />
              <Line
                type="monotone"
                dataKey="limit"
                name="Limite SLA"
                stroke="#ef4444"
                strokeDasharray="5 5"
                dot={false}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="surface-card p-5">
        <div className="mb-4 flex items-end justify-between gap-4">
          <div>
            <h4 className="text-sm font-semibold text-heading">Taux d&apos;erreur</h4>
            <p className="mt-1 text-xs text-muted">Dernier point : {lastPoint.errorRate.toFixed(2)}%</p>
          </div>
          <div className="text-xs text-muted">Objectif : le plus bas possible</div>
        </div>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData}>
              <CartesianGrid
                strokeDasharray="3 8"
                vertical={false}
                stroke="currentColor"
                className="text-border/70"
              />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} hide />
              <YAxis tick={{ fontSize: 11 }} axisLine={false} tickLine={false} className="text-muted" />
              <Tooltip content={<TooltipContent />} />
              <Area
                type="monotone"
                dataKey="errorRate"
                name="Erreurs"
                stroke="#06b6d4"
                fill="#06b6d4"
                fillOpacity={0.18}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
