"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Plus } from "lucide-react";
import { IncidentFormModal } from "@/components/forms/IncidentFormModal";
import { Header } from "@/components/layout/Header";
import { SlaMetricsCharts } from "@/components/sla/SlaMetricsCharts";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatPercent } from "@/lib/utils";
import type { Incident, MonitoringMetric, Sla } from "@/types";

export default function SlaDetailPage() {
  const params = useParams();
  const slaId = Number(params.id);
  const { isAdmin } = useAuth();
  const [sla, setSla] = useState<Sla | null>(null);
  const [metrics, setMetrics] = useState<MonitoringMetric[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [incidentModalOpen, setIncidentModalOpen] = useState(false);

  const loadData = useCallback(async () => {
    if (!slaId) return;
    setLoading(true);
    setError(null);
    try {
      const [slaData, metricsData, incidentsData] = await Promise.all([
        apiFetch<Sla>(`/api/slas/${slaId}`),
        apiFetch<MonitoringMetric[]>(`/api/metrics?slaId=${slaId}`),
        apiFetch<Incident[]>(`/api/incidents?slaId=${slaId}`),
      ]);
      setSla(slaData);
      setMetrics(metricsData);
      setIncidents(incidentsData);
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
    return <div className="py-20 text-center text-muted">Chargement du SLA...</div>;
  }

  if (!sla) {
    return (
      <div className="py-20 text-center">
        <p className="text-muted">SLA introuvable.</p>
        <Link href="/slas" className="mt-4 inline-block text-emerald-600">
          Retour à la liste
        </Link>
      </div>
    );
  }

  return (
    <>
      <div className="mb-6">
        <Link
          href="/slas"
          className="inline-flex items-center gap-2 text-sm text-muted hover:text-emerald-600"
        >
          <ArrowLeft className="h-4 w-4" />
          Retour aux SLA
        </Link>
      </div>

      <Header
        title={sla.name}
        description={`Contrat SLA #${sla.id} — client #${sla.clientId}`}
        action={
          isAdmin ? (
            <Button onClick={() => setIncidentModalOpen(true)}>
              <Plus className="h-4 w-4" />
              Nouvel incident
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadData} />}

      <div className="mb-6 grid gap-4 md:grid-cols-4">
        {[
          { label: "Statut", value: <StatusBadge status={sla.status} /> },
          { label: "Uptime cible", value: formatPercent(sla.uptimeTarget) },
          { label: "Temps réponse max", value: `${sla.responseTimeLimit} ms` },
          { label: "Taux erreur max", value: formatPercent(sla.errorRateLimit) },
        ].map((item) => (
          <Card key={item.label}>
            <CardBody>
              <p className="text-sm text-muted">{item.label}</p>
              <div className="mt-2 text-lg font-semibold text-heading">{item.value}</div>
            </CardBody>
          </Card>
        ))}
      </div>

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
                  <td className="px-6 py-4 text-body">{incident.severity}</td>
                  <td className="max-w-md px-6 py-4 text-body">{incident.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {incidents.length === 0 && (
            <div className="px-6 py-10 text-sm text-muted">Aucun incident enregistré.</div>
          )}
        </CardBody>
      </Card>

      {isAdmin && (
        <IncidentFormModal
          open={incidentModalOpen}
          onClose={() => setIncidentModalOpen(false)}
          onSaved={loadData}
          defaultSlaId={sla.id}
        />
      )}
    </>
  );
}
