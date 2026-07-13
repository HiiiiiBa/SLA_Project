"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Activity,
  FolderKanban,
  ShieldCheck,
  Siren,
} from "lucide-react";
import { Header } from "@/components/layout/Header";
import { StatCard } from "@/components/dashboard/StatCard";
import { ProjectOverviewPanel } from "@/components/dashboard/ProjectOverviewPanel";
import { AvailabilityTrendChart } from "@/components/dashboard/AvailabilityTrendChart";
import { ServiceHealthChart } from "@/components/dashboard/ServiceHealthChart";
import { SlaStatusChart } from "@/components/dashboard/SlaStatusChart";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import {
  buildAvailabilityTrend,
  buildServiceHealthDistribution,
  computeDashboardKpis,
} from "@/lib/dashboard-metrics";
import { formatDate } from "@/lib/utils";
import type { Alert, Incident, Project, Report, ServiceEntity, Sla } from "@/types";

export default function DashboardPage() {
  const { hasGlobalDashboard, isManager, isClient, canDownloadReports } = useAuth();
  const sessionUserId = useSessionUserId();
  const [slas, setSlas] = useState<Sla[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);

    try {
      const results = await Promise.allSettled([
        apiFetch<Sla[]>("/api/slas"),
        apiFetch<Alert[]>("/api/alerts"),
        apiFetch<Project[]>("/api/projects"),
        apiFetch<Incident[]>("/api/incidents"),
        apiFetch<ServiceEntity[]>("/api/services"),
        canDownloadReports
          ? apiFetch<Report[]>("/api/reports")
          : Promise.resolve([] as Report[]),
      ]);

      const labels = ["SLA", "alertes", "projets", "incidents", "services", "rapports"];
      const failures = results
        .map((result, index) => (result.status === "rejected" ? labels[index] : null))
        .filter(Boolean);

      if (failures.length > 0) {
        const firstError = results.find(
          (result): result is PromiseRejectedResult => result.status === "rejected",
        )?.reason;
        const detail =
          firstError instanceof ApiError
            ? firstError.message
            : "Erreur de communication avec le serveur";
        setError(`${detail} (${failures.join(", ")})`);
      }

      const [slaResult, alertResult, projectResult, incidentResult, serviceResult, reportResult] =
        results;

      if (slaResult.status === "fulfilled") setSlas(slaResult.value);
      if (alertResult.status === "fulfilled") setAlerts(alertResult.value);
      if (projectResult.status === "fulfilled") setProjects(projectResult.value);
      if (incidentResult.status === "fulfilled") setIncidents(incidentResult.value);
      if (serviceResult.status === "fulfilled") setServices(serviceResult.value);
      if (reportResult.status === "fulfilled") setReports(reportResult.value);
      else setReports([]);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Erreur de communication avec le serveur",
      );
    } finally {
      setLoading(false);
    }
  }, [sessionUserId, canDownloadReports]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const clientsCount = useMemo(
    () => new Set(projects.map((p) => p.clientId)).size,
    [projects],
  );

  const kpis = useMemo(
    () =>
      computeDashboardKpis({
        clientsCount,
        projects,
        services,
        incidents,
        alerts,
        slas,
        reports,
      }),
    [clientsCount, projects, services, incidents, alerts, slas, reports],
  );

  const availabilityTrend = useMemo(
    () => buildAvailabilityTrend(kpis.avgAvailability, 30),
    [kpis.avgAvailability],
  );
  const serviceHealth = useMemo(
    () => buildServiceHealthDistribution(services, alerts),
    [services, alerts],
  );

  const recentAlerts = useMemo(
    () =>
      [...alerts]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5),
    [alerts],
  );

  return (
    <>
      <Header
        title="Tableau de bord"
        description={
          hasGlobalDashboard
            ? "Vue supervision — indicateurs clés et projets."
            : isManager
              ? "Supervision de vos clients et projets."
              : isClient
                ? "Vue de vos projets et contrats SLA."
                : "Supervision de vos projets assignés."
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadData} />}

      {loading ? (
        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <div
                key={`kpi-${index}`}
                className="h-32 animate-pulse rounded-2xl bg-card/60 ring-1 ring-border/50"
              />
            ))}
          </div>
          <div className="grid gap-6 xl:grid-cols-3">
            <div className="h-80 animate-pulse rounded-2xl bg-card/60 ring-1 ring-border/50 xl:col-span-2" />
            <div className="h-80 animate-pulse rounded-2xl bg-card/60 ring-1 ring-border/50" />
          </div>
        </div>
      ) : (
        <div className="space-y-8">
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard
              title="Projets"
              value={kpis.projectsCount}
              hint={`${clientsCount} client(s)`}
              icon={<FolderKanban className="h-6 w-6" />}
              accent="cyan"
            />
            <StatCard
              title="Disponibilité"
              value={`${kpis.avgAvailability.toLocaleString("fr-FR")}%`}
              hint="Moyenne"
              icon={<Activity className="h-6 w-6" />}
              accent="emerald"
            />
            <StatCard
              title="Incidents ouverts"
              value={kpis.openIncidents}
              hint={`${kpis.criticalIncidents} critique(s)`}
              icon={<Siren className="h-6 w-6" />}
              accent="amber"
            />
            <StatCard
              title="SLA respectés"
              value={`${kpis.slaRespectedPct.toLocaleString("fr-FR")}%`}
              hint={`${kpis.activeAlerts} alerte(s) active(s)`}
              icon={<ShieldCheck className="h-6 w-6" />}
              accent="cyan"
            />
          </section>

          <section className="grid gap-6 xl:grid-cols-3">
            <div className="grid gap-6 lg:grid-cols-2 xl:col-span-2">
              <Card>
                <CardHeader
                  title="Disponibilité"
                  description="30 derniers jours"
                />
                <CardBody>
                  <AvailabilityTrendChart data={availabilityTrend} />
                </CardBody>
              </Card>

              <Card>
                <CardHeader
                  title="État des services"
                  description="UP / DEGRADED / DOWN"
                />
                <CardBody>
                  <ServiceHealthChart data={serviceHealth} />
                </CardBody>
              </Card>

              <Card className="lg:col-span-2">
                <CardHeader
                  title="Statuts SLA"
                  description="Répartition actuelle"
                />
                <CardBody>
                  <SlaStatusChart slas={slas} />
                </CardBody>
              </Card>
            </div>

            <Card className="flex flex-col">
              <CardHeader
                title="Alertes récentes"
                description={`${kpis.activeAlerts} active(s)`}
              />
              <CardBody className="scroll-area min-h-0 flex-1 space-y-3 overflow-y-auto p-4 pt-0">
                {recentAlerts.length === 0 ? (
                  <p className="py-8 text-center text-sm text-muted">Aucune alerte.</p>
                ) : (
                  recentAlerts.map((alert) => (
                    <div
                      key={alert.id}
                      className="rounded-xl border border-border/60 bg-card/50 p-3"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <StatusBadge status={alert.status} kind="alert" />
                        <span className="text-xs text-muted">{formatDate(alert.createdAt)}</span>
                      </div>
                      <p className="mt-2 line-clamp-2 text-sm text-body">{alert.message}</p>
                    </div>
                  ))
                )}
              </CardBody>
            </Card>
          </section>

          <ProjectOverviewPanel
            projects={projects}
            slas={slas}
            alerts={alerts}
            incidents={incidents}
            reports={reports}
          />
        </div>
      )}
    </>
  );
}
