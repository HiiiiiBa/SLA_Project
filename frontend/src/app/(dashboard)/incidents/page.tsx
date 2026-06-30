"use client";

import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, Pencil, Plus, Siren, Trash2 } from "lucide-react";
import { IncidentFormModal } from "@/components/forms/IncidentFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { SeverityBadge } from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Incident, IncidentSeverity } from "@/types";

const severities: IncidentSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

export default function IncidentsPage() {
  const { isAdmin } = useAuth();
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);
  const [filterSeverity, setFilterSeverity] = useState("");

  const loadIncidents = useCallback(() => {
    setLoading(true);
    setError(null);
    const query = filterSeverity ? `?severity=${filterSeverity}` : "";
    apiFetch<Incident[]>(`/api/incidents${query}`)
      .then(setIncidents)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [filterSeverity]);

  useEffect(() => {
    loadIncidents();
  }, [loadIncidents]);

  async function handleDelete(incident: Incident) {
    if (!confirm("Supprimer cet incident ?")) return;
    try {
      await apiFetch<void>(`/api/incidents/${incident.id}`, { method: "DELETE" });
      loadIncidents();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  async function handleClose(incident: Incident) {
    try {
      await apiFetch<Incident>(`/api/incidents/${incident.id}/close`, { method: "PATCH" });
      loadIncidents();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Clôture impossible");
    }
  }

  return (
    <>
      <Header
        title="Incidents"
        description="Suivi des incidents impactant vos contrats SLA."
        action={
          isAdmin ? (
            <Button
              onClick={() => {
                setSelectedIncident(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouvel incident
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadIncidents} />}

      <Card className="mb-6">
        <CardHeader title="Filtres" description="Filtrer par niveau de sévérité" />
        <CardBody>
          <div className="max-w-xs space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted">
              Sévérité
            </label>
            <Select
              value={filterSeverity}
              onChange={(e) => setFilterSeverity(e.target.value)}
            >
              <option value="">Toutes les sévérités</option>
              {severities.map((severity) => (
                <option key={severity} value={severity}>
                  {severity}
                </option>
              ))}
            </Select>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Tous les incidents"
          description={`${incidents.length} incident(s) affiché(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Début</th>
                  <th className="px-6 py-4 font-medium">Fin</th>
                  <th className="px-6 py-4 font-medium">Sévérité</th>
                  <th className="px-6 py-4 font-medium">Description</th>
                  {isAdmin && <th className="px-6 py-4 font-medium">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {incidents.map((incident) => (
                  <tr key={incident.id} className="table-row">
                    <td className="px-6 py-4 text-body">#{incident.slaId}</td>
                    <td className="px-6 py-4 text-body">{formatDate(incident.startTime)}</td>
                    <td className="px-6 py-4 text-body">
                      {incident.endTime ? formatDate(incident.endTime) : "En cours"}
                    </td>
                    <td className="px-6 py-4">
                      <SeverityBadge severity={incident.severity} />
                    </td>
                    <td className="max-w-sm px-6 py-4 text-body">{incident.description}</td>
                    {isAdmin && (
                      <td className="px-6 py-4">
                        <div className="flex gap-2">
                          {!incident.endTime && (
                            <Button variant="secondary" onClick={() => handleClose(incident)}>
                              <CheckCircle2 className="h-4 w-4" />
                            </Button>
                          )}
                          <Button
                            variant="secondary"
                            onClick={() => {
                              setSelectedIncident(incident);
                              setModalOpen(true);
                            }}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button variant="danger" onClick={() => handleDelete(incident)}>
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && incidents.length === 0 && (
            <EmptyState
              icon={Siren}
              title="Aucun incident enregistré"
              description="Les incidents impactent le calcul du score SLA. Créez-en un depuis cette page ou depuis le détail d'un SLA."
            />
          )}
        </CardBody>
      </Card>

      {isAdmin && (
        <IncidentFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadIncidents}
          incident={selectedIncident}
        />
      )}
    </>
  );
}
