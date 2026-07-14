"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { Eye, FolderKanban, Gauge, Pencil, Plus, Trash2 } from "lucide-react";
import { SlaApprovalRequestActions } from "@/components/sla/SlaApprovalRequestActions";
import { SlaFormModal } from "@/components/forms/SlaFormModal";
import { SlaLifecycleActions } from "@/components/sla/SlaLifecycleActions";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { StatusBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatPercent } from "@/lib/utils";
import type { Sla } from "@/types";

export default function SlasPage() {
  const { isAdmin, isClient, isEmployee, isManager, canManageSla, canManageSlaLifecycle, canRequestApproval } = useAuth();
  const [success, setSuccess] = useState<string | null>(null);
  const sessionUserId = useSessionUserId();
  const [slas, setSlas] = useState<Sla[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedSla, setSelectedSla] = useState<Sla | null>(null);

  const loadSlas = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    apiFetch<Sla[]>("/api/slas")
      .then(setSlas)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId]);

  useEffect(() => {
    loadSlas();
  }, [loadSlas]);

  async function handleDelete(sla: Sla) {
    if (!confirm(`Supprimer le SLA "${sla.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/slas/${sla.id}`, { method: "DELETE" });
      loadSlas();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Contrats SLA"
        description={
          isClient
            ? "Consultation de vos contrats SLA (lecture seule)."
            : isEmployee
              ? "SLA des clients liés à vos projets assignés."
              : isManager
                ? "SLA de vos clients affectés — création et modification autorisées."
                : "Liste des accords de niveau de service monitorés et leurs seuils."
        }
        action={
          canManageSla ? (
            <Button
              onClick={() => {
                setSelectedSla(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouveau SLA
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadSlas} />}
      {success && (
        <div className="mb-4 rounded-xl border border-success/30 bg-success/10 px-4 py-3 text-sm text-success">
          {success}
        </div>
      )}

      <Card>
        <CardHeader
          title="Tous les SLA"
          description={`${slas.length} contrat(s) enregistré(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">Uptime cible</th>
                  <th className="px-6 py-4 font-medium">Temps réponse max</th>
                  <th className="px-6 py-4 font-medium">Taux erreur max</th>
                  <th className="px-6 py-4 font-medium">Client</th>
                  <th className="px-6 py-4 font-medium">Projet(s)</th>
                  <th className="px-6 py-4 font-medium">Services</th>
                  <th className="min-w-[11rem] whitespace-nowrap px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {slas.map((sla) => (
                  <tr key={sla.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{sla.name}</td>
                    <td className="px-6 py-4">
                      <StatusBadge status={sla.status} />
                    </td>
                    <td className="px-6 py-4 text-body">{formatPercent(sla.uptimeTarget)}</td>
                    <td className="px-6 py-4 text-body">{sla.responseTimeLimit} ms</td>
                    <td className="px-6 py-4 text-body">{formatPercent(sla.errorRateLimit)}</td>
                    <td className="px-6 py-4 text-body">
                      {sla.clientName ?? `Client #${sla.clientId}`}
                    </td>
                    <td className="px-6 py-4 text-body">
                      {sla.linkedProjects && sla.linkedProjects.length > 0 ? (
                        <div className="flex flex-wrap gap-1.5">
                          {sla.linkedProjects.map((project) => (
                            <Link
                              key={project.id}
                              href={`/projects/${project.id}`}
                              className="inline-flex items-center gap-1 rounded-lg border border-border/70 bg-card/60 px-2 py-0.5 text-xs font-medium text-heading transition hover:border-primary/40 hover:text-primary"
                              title={project.name}
                            >
                              <FolderKanban className="h-3 w-3 shrink-0 text-muted" />
                              {project.name}
                            </Link>
                          ))}
                        </div>
                      ) : (
                        <span className="text-muted">Non assigné</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-body">{sla.serviceCount ?? 0}</td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <div className="inline-flex items-center gap-1.5">
                        <Link href={`/slas/${sla.id}`}>
                          <Button variant="secondary" className="!px-2.5 !py-2" title="Voir">
                            <Eye className="h-4 w-4" />
                          </Button>
                        </Link>
                        {(isAdmin || isManager) && (
                          <>
                            <Button
                              variant="secondary"
                              className="!px-2.5 !py-2"
                              title="Modifier"
                              onClick={() => {
                                setSelectedSla(sla);
                                setModalOpen(true);
                              }}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            {isAdmin && (
                              <Button
                                variant="danger"
                                className="!px-2.5 !py-2"
                                title="Supprimer"
                                onClick={() => handleDelete(sla)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            )}
                            {canManageSlaLifecycle && (
                              <SlaLifecycleActions
                                sla={sla}
                                onChanged={loadSlas}
                                onError={setError}
                                compact
                              />
                            )}
                            {canRequestApproval && (
                              <SlaApprovalRequestActions
                                sla={sla}
                                onError={setError}
                                onSuccess={() => {
                                  setSuccess(`Demande de suppression envoyée pour le SLA "${sla.name}".`);
                                  setError(null);
                                }}
                                compact
                              />
                            )}
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && slas.length === 0 && (
            <EmptyState
              icon={Gauge}
              title="Aucun contrat SLA"
              description="Créez un SLA pour définir les seuils de performance (uptime, temps de réponse, taux d'erreur)."
            />
          )}
        </CardBody>
      </Card>

      {canManageSla && (
        <SlaFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadSlas}
          sla={selectedSla}
        />
      )}
    </>
  );
}
