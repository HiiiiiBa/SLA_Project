"use client";

import { useCallback, useEffect, useState } from "react";
import {
  AlertTriangle,
  Building2,
  Gauge,
  Siren,
} from "lucide-react";
import { Header } from "@/components/layout/Header";
import { StatCard } from "@/components/dashboard/StatCard";
import { SlaStatusChart } from "@/components/dashboard/SlaStatusChart";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge, SeverityBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Alert, Client, Incident, Project, Report, Sla } from "@/types";

export default function DashboardPage() {
  const { hasGlobalDashboard, isEmployee, isClient, isManager, canDownloadReports } = useAuth();
  const sessionUserId = useSessionUserId();
  const usesProjectDashboard = !hasGlobalDashboard;
  const [slas, setSlas] = useState<Sla[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);

    try {
      const [slaResult, alertResult, thirdResult, incidentResult, reportResult] =
        await Promise.allSettled([
          apiFetch<Sla[]>("/api/slas"),
          apiFetch<Alert[]>("/api/alerts"),
          usesProjectDashboard
            ? apiFetch<Project[]>("/api/projects")
            : apiFetch<Client[]>("/api/clients"),
          apiFetch<Incident[]>("/api/incidents"),
          canDownloadReports
            ? apiFetch<Report[]>("/api/reports")
            : Promise.resolve([] as Report[]),
        ]);

      const labels = usesProjectDashboard
        ? ["SLA", "alertes", "projets", "incidents", ...(canDownloadReports ? ["rapports"] : [])]
        : ["SLA", "alertes", "clients", "incidents", ...(canDownloadReports ? ["rapports"] : [])];
      const resultList = [slaResult, alertResult, thirdResult, incidentResult, reportResult];
      const failures = resultList
        .map((result, index) => (result.status === "rejected" ? labels[index] : null))
        .filter(Boolean);

      if (failures.length > 0) {
        const firstError = resultList.find(
          (result): result is PromiseRejectedResult => result.status === "rejected",
        )?.reason;
        const detail =
          firstError instanceof ApiError
            ? firstError.message
            : "Erreur de communication avec le serveur";
        setError(`${detail} (${failures.join(", ")})`);
      }

      if (slaResult.status === "fulfilled") setSlas(slaResult.value);
      if (alertResult.status === "fulfilled") setAlerts(alertResult.value);
      if (reportResult.status === "fulfilled") setReports(reportResult.value);
      else setReports([]);
      if (incidentResult.status === "fulfilled") setIncidents(incidentResult.value);
      if (thirdResult.status === "fulfilled") {
        if (usesProjectDashboard) {
          setProjects(thirdResult.value as Project[]);
          setClients([]);
        } else {
          setClients(thirdResult.value as Client[]);
          setProjects([]);
        }
      }
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Erreur de communication avec le serveur",
      );
    } finally {
      setLoading(false);
    }
  }, [sessionUserId, usesProjectDashboard, canDownloadReports]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const activeSlas = slas.filter((sla) => sla.status === "ACTIVE").length;
  const breachedSlas = slas.filter((sla) => sla.status === "BREACHED").length;
  const openAlerts = alerts.filter((alert) => alert.status === "NEW").length;
  const openIncidents = incidents.filter((incident) => !incident.endTime).length;

  return (
    <>
      <Header
        title="Tableau de bord"
        description={
          hasGlobalDashboard
            ? "Vue globale de la santé de tous les contrats SLA, alertes et rapports."
            : isManager
              ? "Tableau de bord de vos projets et clients affectés."
              : isClient
                ? "Tableau de bord de vos projets et contrats SLA."
                : "Vue d'ensemble de vos projets, SLA et incidents assignés."
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadData} />}

      {loading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div
              key={index}
              className="h-36 animate-pulse rounded-2xl bg-card/60 ring-1 ring-border/50"
            />
          ))}
        </div>
      ) : (
        <>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <StatCard
              title="SLA actifs"
              value={activeSlas}
              hint={`${slas.length} contrats au total`}
              icon={<Gauge className="h-6 w-6" />}
            />
            <StatCard
              title="SLA violés"
              value={breachedSlas}
              hint="Nécessitent une action"
              icon={<AlertTriangle className="h-6 w-6" />}
              accent="red"
            />
            <StatCard
              title="Alertes ouvertes"
              value={openAlerts}
              hint={`${alerts.length} alertes totales`}
              icon={<AlertTriangle className="h-6 w-6" />}
              accent="amber"
            />
            <StatCard
              title="Incidents ouverts"
              value={openIncidents}
              hint={`${incidents.length} incidents au total`}
              icon={<Siren className="h-6 w-6" />}
              accent="blue"
            />
          </div>

          <div className="mt-8 grid gap-6 xl:grid-cols-3">
            <Card className="xl:col-span-2">
              <CardHeader
                title="Répartition des SLA"
                description="Statuts actuels des contrats monitorés"
              />
              <CardBody>
                <SlaStatusChart slas={slas} />
              </CardBody>
            </Card>

            <Card>
              <CardHeader title="Dernières alertes" />
              <CardBody className="space-y-4">
                {alerts.slice(0, 5).map((alert) => (
                  <div
                    key={alert.id}
                    className="rounded-xl border border-border bg-card/50 p-4 backdrop-blur-sm"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <StatusBadge status={alert.status} kind="alert" />
                      <span className="text-xs text-muted">
                        {formatDate(alert.createdAt)}
                      </span>
                    </div>
                    <p className="mt-3 text-sm leading-relaxed text-body">
                      {alert.message}
                    </p>
                  </div>
                ))}
                {alerts.length === 0 && (
                  <p className="text-sm text-muted">Aucune alerte pour le moment.</p>
                )}
              </CardBody>
            </Card>
          </div>

          <div className="mt-6 grid gap-6 lg:grid-cols-2">
            {usesProjectDashboard ? (
              <Card>
                <CardHeader
                  title="Mes projets"
                  description={`${projects.length} projet(s) assigné(s)`}
                />
                <CardBody className="space-y-3">
                  {projects.map((project) => (
                    <div
                      key={project.id}
                      className="flex items-center justify-between rounded-xl border border-border/60 bg-card/50 px-4 py-3"
                    >
                      <div>
                        <p className="font-medium text-heading">{project.name}</p>
                        <p className="text-xs text-muted">{project.clientName}</p>
                      </div>
                      <span className="text-xs text-muted">{project.teamName ?? "—"}</span>
                    </div>
                  ))}
                  {projects.length === 0 && (
                    <p className="text-sm text-muted">Aucun projet assigné.</p>
                  )}
                </CardBody>
              </Card>
            ) : (
              <Card>
                <CardHeader
                  title="Clients suivis"
                  description={`${clients.length} organisations monitorées`}
                />
                <CardBody className="space-y-3">
                  {clients.map((client) => (
                    <div
                      key={client.id}
                      className="flex items-center justify-between rounded-xl border border-border/60 bg-card/50 px-4 py-3"
                    >
                      <div>
                        <p className="font-medium text-heading">{client.name}</p>
                        <p className="text-xs text-muted">{client.projectName}</p>
                      </div>
                      <span className="text-xs text-muted">{client.email}</span>
                    </div>
                  ))}
                  {clients.length === 0 && (
                    <p className="text-sm text-muted">Aucun client enregistré.</p>
                  )}
                </CardBody>
              </Card>
            )}

            <Card>
              <CardHeader
                title="Incidents récents"
                description={`${openIncidents} incident(s) en cours`}
              />
              <CardBody className="space-y-3">
                {incidents.slice(0, 5).map((incident) => (
                  <div
                    key={incident.id}
                    className="rounded-xl border border-border/60 bg-card/50 p-4"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <SeverityBadge severity={incident.severity} />
                      <span className="text-xs text-muted">
                        {formatDate(incident.startTime)}
                      </span>
                    </div>
                    <p className="mt-2 line-clamp-2 text-sm text-body">
                      {incident.description}
                    </p>
                    {!incident.endTime && (
                      <span className="mt-2 inline-block text-xs font-medium text-warning">
                        En cours
                      </span>
                    )}
                  </div>
                ))}
                {incidents.length === 0 && (
                  <p className="text-sm text-muted">Aucun incident enregistré.</p>
                )}
              </CardBody>
            </Card>
          </div>

          <Card className="mt-6">
            <CardHeader
              title="SLA récents"
              description="Contrats monitorés et leurs objectifs"
            />
            <CardBody className="overflow-x-auto p-0">
              <table className="min-w-full text-sm">
                <thead className="table-head">
                  <tr>
                    <th className="px-6 py-4 font-medium">Nom</th>
                    <th className="px-6 py-4 font-medium">Statut</th>
                    <th className="px-6 py-4 font-medium">Uptime cible</th>
                    <th className="px-6 py-4 font-medium">Temps réponse</th>
                    <th className="px-6 py-4 font-medium">Taux erreur</th>
                  </tr>
                </thead>
                <tbody>
                  {slas.slice(0, 8).map((sla) => (
                    <tr key={sla.id} className="table-row">
                      <td className="px-6 py-4 font-medium text-heading">
                        {sla.name}
                      </td>
                      <td className="px-6 py-4">
                        <StatusBadge status={sla.status} />
                      </td>
                      <td className="px-6 py-4 text-body">{sla.uptimeTarget}%</td>
                      <td className="px-6 py-4 text-body">
                        {sla.responseTimeLimit} ms
                      </td>
                      <td className="px-6 py-4 text-body">
                        {sla.errorRateLimit}%
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {slas.length === 0 && (
                <div className="flex items-center gap-3 px-6 py-10 text-sm text-muted">
                  <Building2 className="h-4 w-4" />
                  Aucun SLA enregistré. Créez des données via l&apos;API backend.
                </div>
              )}
            </CardBody>
          </Card>
        </>
      )}
    </>
  );
}
