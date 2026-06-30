"use client";

import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, Pencil, Plus, Trash2 } from "lucide-react";
import { IncidentFormModal } from "@/components/forms/IncidentFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Incident } from "@/types";

export default function IncidentsPage() {
  const { isAdmin } = useAuth();
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);

  const loadIncidents = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<Incident[]>("/api/incidents")
      .then(setIncidents)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

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

      <Card>
        <CardHeader
          title="Tous les incidents"
          description={`${incidents.length} incident(s) enregistré(s)`}
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
                    <td className="px-6 py-4 text-body">{incident.severity}</td>
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
            <div className="px-6 py-10 text-sm text-muted">Aucun incident trouvé.</div>
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
