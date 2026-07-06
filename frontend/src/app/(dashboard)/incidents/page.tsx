"use client";

import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, Eye, Pencil, Plus, Siren } from "lucide-react";
import { IncidentFormModal } from "@/components/forms/IncidentFormModal";
import { IncidentWorkflowModal } from "@/components/forms/IncidentWorkflowModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { SeverityBadge, IncidentStatusBadge } from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Incident, IncidentSeverity, Project } from "@/types";

const severities: IncidentSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

export default function IncidentsPage() {
  const { isAdmin, isClient, isEmployee, isManager, canCreateIncident, canModifyIncident, canAssignIncident } = useAuth();
  const sessionUserId = useSessionUserId();
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [workflowOpen, setWorkflowOpen] = useState(false);
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);
  const [filterSeverity, setFilterSeverity] = useState("");
  const [filterProject, setFilterProject] = useState("");

  useEffect(() => {
    if (!sessionUserId) return;
    apiFetch<Project[]>("/api/projects")
      .then(setProjects)
      .catch(() => setProjects([]));
  }, [sessionUserId]);

  const loadIncidents = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    const useApiSeverity = filterSeverity && !filterProject;
    const useApiProject = filterProject && !filterSeverity;
    const params = new URLSearchParams();
    if (useApiSeverity) params.set("severity", filterSeverity);
    if (useApiProject) params.set("projectId", filterProject);
    const query = params.toString() ? `?${params}` : "";
    apiFetch<Incident[]>(`/api/incidents${query}`)
      .then((data) => {
        let result = data;
        if (filterSeverity) {
          result = result.filter((i) => i.severity === filterSeverity);
        }
        if (filterProject) {
          result = result.filter((i) => String(i.projectId ?? "") === filterProject);
        }
        setIncidents(result);
      })
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId, filterSeverity, filterProject]);

  useEffect(() => {
    loadIncidents();
  }, [loadIncidents]);

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
        description={
          isClient
            ? "Déclarez et consultez les incidents liés à vos projets et SLA."
            : isEmployee
              ? "Incidents qui vous sont assignés par votre manager."
              : isManager
                ? "Suivi des incidents — assignez un employé pour le traitement."
                : "Suivi des incidents impactant vos contrats SLA."
        }
        action={
          canCreateIncident ? (
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
        <CardHeader title="Filtres" description="Filtrer par sévérité ou projet" />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
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
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                Projet
              </label>
              <Select
                value={filterProject}
                onChange={(e) => setFilterProject(e.target.value)}
              >
                <option value="">Tous les projets</option>
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.name} ({project.clientName})
                  </option>
                ))}
              </Select>
            </div>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title={isEmployee ? "Mes incidents assignés" : "Tous les incidents"}
          description={
            isEmployee
              ? `${incidents.length} incident(s) assigné(s) à vous`
              : `${incidents.length} incident(s) affiché(s)`
          }
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Projet</th>
                  <th className="px-6 py-4 font-medium">Début</th>
                  <th className="px-6 py-4 font-medium">Fin</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">Sévérité</th>
                  <th className="px-6 py-4 font-medium">Assigné à</th>
                  <th className="px-6 py-4 font-medium">Description</th>
                  {(canModifyIncident || canAssignIncident) && (
                    <th className="px-6 py-4 font-medium">Actions</th>
                  )}
                </tr>
              </thead>
              <tbody>
                {incidents.map((incident) => (
                  <tr key={incident.id} className="table-row">
                    <td className="px-6 py-4 text-body">#{incident.slaId}</td>
                    <td className="px-6 py-4 text-body">{incident.projectName ?? "—"}</td>
                    <td className="px-6 py-4 text-body">{formatDate(incident.startTime)}</td>
                    <td className="px-6 py-4 text-body">
                      {incident.endTime ? formatDate(incident.endTime) : "—"}
                    </td>
                    <td className="px-6 py-4">
                      <IncidentStatusBadge status={incident.status} />
                    </td>
                    <td className="px-6 py-4">
                      <SeverityBadge severity={incident.severity} />
                    </td>
                    <td className="px-6 py-4 text-body">{incident.assigneeName ?? "—"}</td>
                    <td className="max-w-sm px-6 py-4 text-body">{incident.description}</td>
                    {(canModifyIncident || canAssignIncident) && (
                      <td className="px-6 py-4">
                        <div className="flex gap-2">
                          {(canModifyIncident || canAssignIncident) && (
                            <>
                              <Button
                                variant="secondary"
                                title={canAssignIncident ? "Gérer" : "Traiter"}
                                onClick={() => {
                                  setSelectedIncident(incident);
                                  setWorkflowOpen(true);
                                }}
                              >
                                <Eye className="h-4 w-4" />
                              </Button>
                              {canModifyIncident && !isEmployee && !incident.endTime && incident.status !== "RESOLVED" && (
                                <Button
                                  variant="secondary"
                                  title="Modifier"
                                  onClick={() => {
                                    setSelectedIncident(incident);
                                    if (isAdmin) {
                                      setModalOpen(true);
                                    } else {
                                      setWorkflowOpen(true);
                                    }
                                  }}
                                >
                                  <Pencil className="h-4 w-4" />
                                </Button>
                              )}
                              {isAdmin && !incident.endTime && (
                                <Button variant="secondary" onClick={() => handleClose(incident)}>
                                  <CheckCircle2 className="h-4 w-4" />
                                </Button>
                              )}
                            </>
                          )}
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
              title="Aucun incident"
              description={
                isEmployee
                  ? "Aucun incident ne vous est assigné pour le moment."
                  : "Les incidents impactent le calcul du score SLA. Créez-en un depuis cette page ou depuis le détail d'un SLA."
              }
            />
          )}
        </CardBody>
      </Card>

      {canCreateIncident && (
        <IncidentFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadIncidents}
          incident={isAdmin ? selectedIncident : null}
        />
      )}

      {selectedIncident && (canModifyIncident || canAssignIncident) && (
        <IncidentWorkflowModal
          open={workflowOpen}
          onClose={() => {
            setWorkflowOpen(false);
            setSelectedIncident(null);
          }}
          onSaved={loadIncidents}
          incident={selectedIncident}
        />
      )}
    </>
  );
}
