"use client";

import {
  Area,
  AreaChart,
  CartesianGrid,
  Legend,
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

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <div className="rounded-2xl border border-slate-200 p-4 dark:border-slate-800">
        <h4 className="mb-4 text-sm font-semibold text-heading">Temps de réponse (ms)</h4>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.2} />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} hide />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Legend />
              <Line
                type="monotone"
                dataKey="responseTime"
                name="Réponse"
                stroke="#10b981"
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

      <div className="rounded-2xl border border-slate-200 p-4 dark:border-slate-800">
        <h4 className="mb-4 text-sm font-semibold text-heading">Taux d&apos;erreur (%)</h4>
        <div className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.2} />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} hide />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Area
                type="monotone"
                dataKey="errorRate"
                name="Erreurs"
                stroke="#f59e0b"
                fill="#f59e0b"
                fillOpacity={0.2}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
