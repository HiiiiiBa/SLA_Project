"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import {
  AlertTriangle,
  ArrowLeft,
  Building2,
  FolderKanban,
  Gauge,
  Server,
  Siren,
} from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import {
  IncidentStatusBadge,
  ServiceStatusBadge,
  SeverityBadge,
  StatusBadge,
} from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatPercent } from "@/lib/utils";
import type { Alert, Incident, Project, ServiceEntity, Sla } from "@/types";

export default function ProjectDetailPage() {
  const params = useParams();
  const projectId = Number(params.id);

  const [project, setProject] = useState<Project | null>(null);
  const [sla, setSla] = useState<Sla | null>(null);
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    if (!projectId) return;
    setLoading(true);
    setError(null);

    try {
      const projectResult = await apiFetch<Project>(`/api/projects/${projectId}`);
      setProject(projectResult);

      const [incidentResult, slaResult] = await Promise.allSettled([
        apiFetch<Incident[]>(`/api/incidents?projectId=${projectId}`),
        projectResult.slaId ? apiFetch<Sla>(`/api/slas/${projectResult.slaId}`) : Promise.resolve(null),
      ]);

      if (incidentResult.status === "fulfilled") setIncidents(incidentResult.value);
      else setIncidents([]);

      const loadedSla = slaResult.status === "fulfilled" ? (slaResult.value as Sla | null) : null;
      setSla(loadedSla);

      if (loadedSla?.id) {
        const [serviceResult, alertResult] = await Promise.allSettled([
          apiFetch<ServiceEntity[]>(`/api/services?slaId=${loadedSla.id}`),
          apiFetch<Alert[]>(`/api/alerts?slaId=${loadedSla.id}`),
        ]);

        if (serviceResult.status === "fulfilled") setServices(serviceResult.value);
        else setServices([]);

        if (alertResult.status === "fulfilled") setAlerts(alertResult.value);
        else setAlerts([]);
      } else {
        setServices([]);
        setAlerts([]);
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur de chargement");
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const openIncidents = useMemo(
    () => incidents.filter((i) => i.status !== "RESOLVED"),
    [incidents],
  );
  const activeAlerts = useMemo(
    () => alerts.filter((a) => a.status === "NEW" || a.status === "READ"),
    [alerts],
  );
  const downServices = useMemo(
    () => services.filter((s) => s.status === "DOWN"),
    [services],
  );

  if (loading) {
    return <div className="py-20 text-center text-muted">Chargement du projet...</div>;
  }

  if (!project) {
    return (
      <EmptyState
        icon={FolderKanban}
        title="Projet introuvable"
        description="Ce projet n'existe pas ou a été supprimé."
      />
    );
  }

  return (
    <>
      <div className="mb-6">
        <Link
          href="/projects"
          className="inline-flex items-center gap-2 text-sm text-muted transition hover:text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          Retour aux projets
        </Link>
      </div>

      <Header
        title={project.name}
        description={`${project.clientName}${project.teamName ? ` — ${project.teamName}` : ""}`}
      />

      {error && <ErrorBanner message={error} onRetry={loadData} />}

      <div className="mb-6 grid gap-4 md:grid-cols-4">
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Client</p>
            <p className="mt-2 inline-flex items-center gap-2 text-lg font-semibold text-heading">
              <Building2 className="h-4 w-4 text-muted" />
              {project.clientName}
            </p>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">SLA</p>
            <div className="mt-2">
              {sla ? (
                <div className="flex items-center justify-between gap-3">
                  <Link href={`/slas/${sla.id}`} className="font-semibold text-heading hover:text-primary">
                    {sla.name}
                  </Link>
                  <StatusBadge status={sla.status} />
                </div>
              ) : (
                <p className="text-sm text-muted">Aucun SLA associé</p>
              )}
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Services</p>
            <p className="mt-2 text-2xl font-bold text-heading">{services.length}</p>
            <p className="mt-1 text-xs text-muted">
              {downServices.length > 0 ? `${downServices.length} DOWN` : "Tous UP"}
            </p>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Incidents ouverts</p>
            <p className="mt-2 text-2xl font-bold text-heading">{openIncidents.length}</p>
            <p className="mt-1 text-xs text-muted">
              {activeAlerts.length} alerte(s) active(s)
            </p>
          </CardBody>
        </Card>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <Card>
          <CardHeader
            title="SLA & seuils"
            description="Objectifs et limites contractuelles"
            action={
              sla ? (
                <Link href={`/slas/${sla.id}`}>
                  <Button variant="secondary">Voir le SLA</Button>
                </Link>
              ) : undefined
            }
          />
          <CardBody>
            {sla ? (
              <div className="grid gap-4 sm:grid-cols-3">
                <div className="rounded-xl border border-border/60 bg-card/40 p-4">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted">Uptime cible</p>
                  <p className="mt-2 text-lg font-semibold text-heading">
                    {formatPercent(sla.uptimeTarget)}
                  </p>
                </div>
                <div className="rounded-xl border border-border/60 bg-card/40 p-4">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted">Temps réponse max</p>
                  <p className="mt-2 text-lg font-semibold text-heading">{sla.responseTimeLimit} ms</p>
                </div>
                <div className="rounded-xl border border-border/60 bg-card/40 p-4">
                  <p className="text-xs font-semibold uppercase tracking-wider text-muted">Taux erreur max</p>
                  <p className="mt-2 text-lg font-semibold text-heading">
                    {formatPercent(sla.errorRateLimit)}
                  </p>
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted">Aucun SLA n'est lié à ce projet.</p>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Services"
            description={sla ? `${services.length} service(s) pour ce SLA` : "Services du projet via SLA"}
          />
          <CardBody className="overflow-x-auto p-0">
            {services.length === 0 ? (
              <div className="px-6 py-10 text-sm text-muted">
                {sla ? "Aucun service associé au SLA." : "Aucun SLA associé, donc aucun service."}
              </div>
            ) : (
              <table className="min-w-full text-sm">
                <thead className="table-head">
                  <tr>
                    <th className="px-6 py-4 font-medium">Service</th>
                    <th className="px-6 py-4 font-medium">Statut</th>
                    <th className="px-6 py-4 font-medium">Dernière mise à jour</th>
                  </tr>
                </thead>
                <tbody>
                  {services.map((service) => (
                    <tr key={service.id} className="table-row">
                      <td className="px-6 py-4 font-medium text-heading">
                        <span className="inline-flex items-center gap-2">
                          <Server className="h-4 w-4 text-muted" />
                          {service.name}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <ServiceStatusBadge status={service.status} />
                      </td>
                      <td className="px-6 py-4 text-body">{formatDate(service.updatedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Incidents"
            description={`${openIncidents.length} ouvert(s) · ${incidents.length} total`}
          />
          <CardBody className="overflow-x-auto p-0">
            {incidents.length === 0 ? (
              <div className="px-6 py-10 text-sm text-muted">Aucun incident pour ce projet.</div>
            ) : (
              <table className="min-w-full text-sm">
                <thead className="table-head">
                  <tr>
                    <th className="px-6 py-4 font-medium">Sévérité</th>
                    <th className="px-6 py-4 font-medium">Statut</th>
                    <th className="px-6 py-4 font-medium">Début</th>
                    <th className="px-6 py-4 font-medium">Description</th>
                  </tr>
                </thead>
                <tbody>
                  {incidents.map((incident) => (
                    <tr key={incident.id} className="table-row">
                      <td className="px-6 py-4">
                        <SeverityBadge severity={incident.severity} />
                      </td>
                      <td className="px-6 py-4">
                        <IncidentStatusBadge status={incident.status} />
                      </td>
                      <td className="px-6 py-4 text-body">{formatDate(incident.startTime)}</td>
                      <td className="px-6 py-4 text-body">
                        <span className="inline-flex items-start gap-2">
                          <Siren className="mt-0.5 h-4 w-4 shrink-0 text-muted" />
                          <span className="line-clamp-2">{incident.description}</span>
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Alertes"
            description={`${activeAlerts.length} active(s) · ${alerts.length} total`}
          />
          <CardBody className="p-0">
            {alerts.length === 0 ? (
              <div className="px-6 py-10 text-sm text-muted">Aucune alerte pour ce projet.</div>
            ) : (
              <div className="scroll-area max-h-80 overflow-y-auto overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead className="table-head sticky top-0 z-10">
                    <tr>
                      <th className="px-6 py-4 font-medium">Statut</th>
                      <th className="px-6 py-4 font-medium">Date</th>
                      <th className="px-6 py-4 font-medium">Message</th>
                    </tr>
                  </thead>
                  <tbody>
                    {alerts.map((alert) => (
                      <tr key={alert.id} className="table-row">
                        <td className="px-6 py-4">
                          <StatusBadge status={alert.status} kind="alert" />
                        </td>
                        <td className="px-6 py-4 text-body">{formatDate(alert.createdAt)}</td>
                        <td className="px-6 py-4 text-body">
                          <span className="inline-flex items-start gap-2">
                            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-muted" />
                            <span className="line-clamp-2">{alert.message}</span>
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardBody>
        </Card>
      </div>
    </>
  );
}

