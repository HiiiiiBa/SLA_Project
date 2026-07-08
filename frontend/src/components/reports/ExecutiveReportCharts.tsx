"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { ExecutiveReportKpiSummary } from "@/types";

interface ExecutiveReportChartsProps {
  kpi: ExecutiveReportKpiSummary;
}

function ChartTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: Array<{ value?: number; name?: string; color?: string }>;
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-xl border border-border bg-card/95 px-4 py-3 text-sm shadow-xl backdrop-blur">
      {label && <p className="mb-1 font-semibold text-heading">{label}</p>}
      {payload.map((entry, index) => (
        <div key={index} className="flex items-center justify-between gap-4 text-body">
          <span className="inline-flex items-center gap-2">
            <span
              className="h-2.5 w-2.5 rounded-full"
              style={{ background: entry.color ?? "#94a3b8" }}
            />
            {entry.name}
          </span>
          <span className="font-semibold text-heading">{entry.value}</span>
        </div>
      ))}
    </div>
  );
}

export function ExecutiveReportCharts({ kpi }: ExecutiveReportChartsProps) {
  const complianceData = [
    { name: "Score SLA", value: Number(kpi.slaScore?.toFixed?.(1) ?? kpi.slaScore ?? 0), fill: "#4f46e5" },
    {
      name: "Uptime",
      value: Number(kpi.uptimePercentage?.toFixed?.(1) ?? kpi.uptimePercentage ?? 0),
      fill: "#10b981",
    },
    {
      name: "Cible uptime",
      value: Number(kpi.uptimeTarget?.toFixed?.(1) ?? kpi.uptimeTarget ?? 0),
      fill: "#94a3b8",
    },
    {
      name: "Conf. RT",
      value: Number(
        kpi.responseTimeCompliance?.toFixed?.(1) ?? kpi.responseTimeCompliance ?? 0,
      ),
      fill: "#3b82f6",
    },
  ];

  const volumeData = [
    { name: "Incidents", value: kpi.incidentCount ?? 0, fill: "#f97316" },
    { name: "Critiques", value: kpi.criticalIncidentCount ?? 0, fill: "#ef4444" },
    { name: "Alertes", value: kpi.alertCount ?? 0, fill: "#eab308" },
  ];

  const serviceData = [
    { name: "DOWN", value: kpi.servicesDown ?? 0, fill: "#dc2626" },
    { name: "Dégradés", value: kpi.servicesDegraded ?? 0, fill: "#a855f7" },
  ].filter((item) => item.value > 0);

  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <div className="rounded-xl border border-border/60 bg-card/40 p-4">
        <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted">
          Conformité vs cibles (%)
        </p>
        <div className="h-56 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={complianceData} barSize={36}>
              <CartesianGrid
                strokeDasharray="3 8"
                vertical={false}
                stroke="currentColor"
                className="text-border/70"
              />
              <XAxis
                dataKey="name"
                tick={{ fill: "currentColor", fontSize: 11 }}
                className="text-muted"
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                domain={[0, 100]}
                tick={{ fill: "currentColor", fontSize: 11 }}
                className="text-muted"
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<ChartTooltip />} cursor={{ fill: "rgba(148, 163, 184, 0.08)" }} />
              <Bar dataKey="value" name="Valeur" radius={[10, 10, 0, 0]}>
                {complianceData.map((entry) => (
                  <Cell key={entry.name} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="rounded-xl border border-border/60 bg-card/40 p-4">
        <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted">
          Incidents & alertes
        </p>
        <div className="h-56 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={volumeData} barSize={40}>
              <CartesianGrid
                strokeDasharray="3 8"
                vertical={false}
                stroke="currentColor"
                className="text-border/70"
              />
              <XAxis
                dataKey="name"
                tick={{ fill: "currentColor", fontSize: 11 }}
                className="text-muted"
                axisLine={false}
                tickLine={false}
              />
              <YAxis
                allowDecimals={false}
                tick={{ fill: "currentColor", fontSize: 11 }}
                className="text-muted"
                axisLine={false}
                tickLine={false}
              />
              <Tooltip content={<ChartTooltip />} cursor={{ fill: "rgba(148, 163, 184, 0.08)" }} />
              <Bar dataKey="value" name="Nombre" radius={[10, 10, 0, 0]}>
                {volumeData.map((entry) => (
                  <Cell key={entry.name} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {serviceData.length > 0 && (
        <div className="rounded-xl border border-border/60 bg-card/40 p-4 lg:col-span-2">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted">
            État des services
          </p>
          <div className="mx-auto h-56 w-full max-w-md">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={serviceData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                >
                  {serviceData.map((entry) => (
                    <Cell key={entry.name} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip content={<ChartTooltip />} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
}
