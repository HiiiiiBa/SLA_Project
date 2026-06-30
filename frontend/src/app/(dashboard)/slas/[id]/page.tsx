"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Gauge, Plus, Server, Siren } from "lucide-react";
import { IncidentFormModal } from "@/components/forms/IncidentFormModal";
import { ServiceFormModal } from "@/components/forms/ServiceFormModal";
import { SlaLifecycleActions } from "@/components/sla/SlaLifecycleActions";
import { Header } from "@/components/layout/Header";
import { SlaMetricsCharts } from "@/components/sla/SlaMetricsCharts";
import { Button } from "@/components/ui/Button";
import { SeverityBadge, ServiceStatusBadge, StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatPercent } from "@/lib/utils";
import type { Incident, MonitoringMetric, ServiceEntity, Sla } from "@/types";

export default function SlaDetailPage() {
  const params = useParams();
  const slaId = Number(params.id);
  const { isAdmin } = useAuth();
  const [sla, setSla] = useState<Sla | null>(null);
  const [metrics, setMetrics] = useState<MonitoringMetric[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [incidentModalOpen, setIncidentModalOpen] = useState(false);
  const [serviceModalOpen, setServiceModalOpen] = useState(false);

  const loadData = useCallback(async () => {
    if (!slaId) return;
    setLoading(true);
    setError(null);
    try {
      const [slaData, metricsData, incidentsData, servicesData] = await Promise.all([
        apiFetch<Sla>(`/api/slas/${slaId}`),
        apiFetch<MonitoringMetric[]>(`/api/metrics?slaId=${slaId}`),
        apiFetch<Incident[]>(`/api/incidents?slaId=${slaId}`),
        apiFetch<ServiceEntity[]>(`/api/services?slaId=${slaId}`),
      ]);
      setSla(slaData);
      setMetrics(metricsData);
      setIncidents(incidentsData);
      setServices(servicesData);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur de chargement");
    } finally {
      setLoading(false);
    }
  }, [slaId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  if (loading) {
    return (
      <div className="grid gap-4 py-8 md:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-28 animate-pulse rounded-2xl bg-card/60 ring-1 ring-border/50" />
        ))}
      </div>
    );
  }

  if (!sla) {
    return (
      <div>
        <EmptyState
          icon={Gauge}
          title="SLA introuvable"
          description="Ce contrat n'existe pas ou a été supprimé."
        />
        <div className="text-center">
          <Link href="/slas" className="text-sm font-medium text-primary hover:underline">
            Retour à la liste des SLA
          </Link>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="mb-6">
        <Link
          href="/slas"
          className="inline-flex items-center gap-2 text-sm text-muted transition hover:text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          Retour aux SLA
        </Link>
      </div>

      <Header
        title={sla.name}
        description={`Contrat SLA #${sla.id} — ${sla.clientName ?? `client #${sla.clientId}`}`}
        action={
          isAdmin ? (
            <div className="flex flex-wrap gap-2">
              <Button onClick={() => setServiceModalOpen(true)}>
                <Plus className="h-4 w-4" />
                Associer un service
              </Button>
              <Button variant="secondary" onClick={() => setIncidentModalOpen(true)}>
                <Plus className="h-4 w-4" />
                Nouvel incident
              </Button>
            </div>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadData} />}

      {isAdmin && (
        <SlaLifecycleActions sla={sla} onChanged={loadData} onError={setError} />
      )}

      <div className="mb-6 mt-4 grid gap-4 md:grid-cols-4">
        {[
          { label: "Statut", value: <StatusBadge status={sla.status} /> },
          { label: "Uptime cible", value: formatPercent(sla.uptimeTarget) },
          { label: "Temps réponse max", value: `${sla.responseTimeLimit} ms` },
          { label: "Taux erreur max", value: formatPercent(sla.errorRateLimit) },
        ].map((item) => (
          <Card key={item.label} className="surface-card-interactive">
            <CardBody>
              <p className="text-xs font-semibold uppercase tracking-wider text-muted">{item.label}</p>
              <div className="mt-3 text-lg font-bold text-heading">{item.value}</div>
            </CardBody>
          </Card>
        ))}
      </div>

      <Card className="mb-6">
        <CardHeader
          title="Services associés"
          description={`${services.length} service(s) monitoré(s) pour ce SLA`}
        />
        <CardBody className="overflow-x-auto p-0">
          <table className="min-w-full text-sm">
            <thead className="table-head">
              <tr>
                <th className="px-6 py-4 font-medium">Nom</th>
                <th className="px-6 py-4 font-medium">Statut</th>
                <th className="px-6 py-4 font-medium">Mis à jour</th>
              </tr>
            </thead>
            <tbody>
              {services.map((service) => (
                <tr key={service.id} className="table-row">
                  <td className="px-6 py-4 font-medium text-heading">{service.name}</td>
                  <td className="px-6 py-4">
                    <ServiceStatusBadge status={service.status} />
                  </td>
                  <td className="px-6 py-4 text-muted">{formatDate(service.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {services.length === 0 && (
            <EmptyState
              icon={Server}
              title="Aucun service associé"
              description="Associez des services techniques à ce SLA pour alimenter les métriques et graphiques."
            />
          )}
        </CardBody>
      </Card>

      <Card className="mb-6">
        <CardHeader
          title="Métriques de monitoring"
          description={`${metrics.length} point(s) de mesure`}
        />
        <CardBody>
          <SlaMetricsCharts metrics={metrics} responseTimeLimit={sla.responseTimeLimit} />
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Incidents associés"
          description={`${incidents.length} incident(s) sur la période`}
        />
        <CardBody className="overflow-x-auto p-0">
          <table className="min-w-full text-sm">
            <thead className="table-head">
              <tr>
                <th className="px-6 py-4 font-medium">Début</th>
                <th className="px-6 py-4 font-medium">Fin</th>
                <th className="px-6 py-4 font-medium">Sévérité</th>
                <th className="px-6 py-4 font-medium">Description</th>
              </tr>
            </thead>
            <tbody>
              {incidents.map((incident) => (
                <tr key={incident.id} className="table-row">
                  <td className="px-6 py-4 text-body">{formatDate(incident.startTime)}</td>
                  <td className="px-6 py-4 text-body">
                    {incident.endTime ? formatDate(incident.endTime) : "En cours"}
                  </td>
                  <td className="px-6 py-4">
                    <SeverityBadge severity={incident.severity} />
                  </td>
                  <td className="max-w-md px-6 py-4 text-body">{incident.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {incidents.length === 0 && (
            <EmptyState
              icon={Siren}
              title="Aucun incident sur ce SLA"
              description="Les incidents enregistrés ici influencent directement le score de conformité."
            />
          )}
        </CardBody>
      </Card>

      {isAdmin && (
        <>
          <IncidentFormModal
            open={incidentModalOpen}
            onClose={() => setIncidentModalOpen(false)}
            onSaved={loadData}
            defaultSlaId={sla.id}
          />
          <ServiceFormModal
            open={serviceModalOpen}
            onClose={() => setServiceModalOpen(false)}
            onSaved={loadData}
            defaultSlaId={sla.id}
          />
        </>
      )}
    </>
  );
}
