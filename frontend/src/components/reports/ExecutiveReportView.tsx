"use client";

import { Bot, FileDown, Lightbulb, TrendingUp } from "lucide-react";
import { ExecutiveReportCharts } from "@/components/reports/ExecutiveReportCharts";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { formatDate, formatScore } from "@/lib/utils";
import type { ExecutiveReport } from "@/types";

interface ExecutiveReportViewProps {
  report: ExecutiveReport;
  onExportPdf: () => void;
  exporting?: boolean;
  onClose?: () => void;
}

function Section({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section className="space-y-2">
      <h3 className="text-sm font-semibold uppercase tracking-wider text-muted">{title}</h3>
      <div className="rounded-xl border border-border/60 bg-card/40 p-4 text-sm leading-relaxed text-body">
        {children}
      </div>
    </section>
  );
}

export function ExecutiveReportView({
  report,
  onExportPdf,
  exporting,
  onClose,
}: ExecutiveReportViewProps) {
  const kpi = report.kpiSummary;

  return (
    <Card className="border-primary/20 shadow-lg shadow-primary/5">
      <CardHeader
        title={report.id ? `AI Executive Report #${report.id}` : "AI Executive Report"}
        description={`${report.projectName} · ${report.clientName} · ${formatDate(report.periodStart)} → ${formatDate(report.periodEnd)}`}
        action={
          <div className="flex flex-wrap gap-2">
            <Button variant="secondary" loading={exporting} onClick={onExportPdf}>
              <FileDown className="h-4 w-4" />
              Exporter PDF
            </Button>
            {onClose && (
              <Button variant="ghost" onClick={onClose}>
                Fermer
              </Button>
            )}
          </div>
        }
      />
      <CardBody className="space-y-6">
        <div className="flex items-start gap-3 rounded-xl border border-primary/20 bg-primary/5 px-4 py-3">
          <Bot className="mt-0.5 h-5 w-5 shrink-0 text-primary" />
          <div>
            <p className="text-sm font-semibold text-heading">Rapport exécutif IA</p>
            <p className="text-xs text-muted">
              SLA {report.slaName} · {formatDate(report.generatedAt)}
              {report.generatedByName ? ` · ${report.generatedByName}` : ""}
            </p>
          </div>
        </div>

        {kpi && (
          <>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <KpiTile label="Score SLA" value={formatScore(kpi.slaScore)} hint={kpi.slaStatus} />
              <KpiTile
                label="Disponibilité"
                value={`${kpi.uptimePercentage?.toFixed(2) ?? "—"}%`}
                hint={`cible ${kpi.uptimeTarget?.toFixed(2) ?? "—"}%`}
              />
              <KpiTile
                label="Temps de réponse"
                value={`${kpi.averageResponseTime?.toFixed(0) ?? "—"} ms`}
                hint={`conformité ${kpi.responseTimeCompliance?.toFixed(1) ?? "—"}%`}
              />
              <KpiTile
                label="Incidents"
                value={String(kpi.incidentCount ?? 0)}
                hint={`${kpi.criticalIncidentCount ?? 0} critique(s) · ${kpi.alertCount ?? 0} alerte(s)`}
              />
              <KpiTile
                label="Taux d'erreur"
                value={`${kpi.averageErrorRate?.toFixed(2) ?? "—"}%`}
                hint={kpi.errorRateLimit != null ? `limite ${kpi.errorRateLimit}%` : undefined}
              />
              <KpiTile
                label="Services"
                value={`${kpi.servicesDown ?? 0} DOWN`}
                hint={`${kpi.servicesDegraded ?? 0} dégradé(s)`}
              />
            </div>

            <section className="space-y-2">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted">
                Graphiques
              </h3>
              <ExecutiveReportCharts kpi={kpi} />
            </section>
          </>
        )}

        <Section title="Executive Summary">{report.executiveSummary}</Section>
        <Section title="KPI Summary">{report.kpiAnalysis}</Section>
        <Section title="Incident Analysis">{report.incidentAnalysis}</Section>

        <Section title="Performance Trends">
          <div className="flex gap-3">
            <TrendingUp className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
            <p>{report.performanceTrends}</p>
          </div>
        </Section>

        <Section title="AI Recommendations">
          <ul className="space-y-2">
            {(report.recommendations ?? []).map((item, index) => (
              <li key={index} className="flex gap-3">
                <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary/15 text-xs font-bold text-primary">
                  {index + 1}
                </span>
                <span className="flex gap-2">
                  <Lightbulb className="mt-0.5 hidden h-4 w-4 shrink-0 text-accent sm:block" />
                  {item}
                </span>
              </li>
            ))}
          </ul>
        </Section>

        <Section title="Overall Conclusion">{report.overallConclusion}</Section>
      </CardBody>
    </Card>
  );
}

function KpiTile({
  label,
  value,
  hint,
}: {
  label: string;
  value: string;
  hint?: string;
}) {
  return (
    <div className="rounded-xl border border-border/60 bg-gradient-to-br from-card to-card/40 px-4 py-3">
      <p className="text-xs font-semibold uppercase tracking-wider text-muted">{label}</p>
      <p className="mt-1 text-lg font-bold text-heading">{value}</p>
      {hint && <p className="mt-0.5 text-xs text-muted">{hint}</p>}
    </div>
  );
}
