"use client";

import { useCallback, useEffect, useState } from "react";
import { CalendarClock, Pencil, Plus, XCircle } from "lucide-react";
import { MaintenanceWindowFormModal } from "@/components/forms/MaintenanceWindowFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { MaintenanceStatusBadge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { MaintenanceWindow, MaintenanceWindowStatus, Sla } from "@/types";

export default function MaintenancePage() {
  const { canManageMaintenance, isClient } = useAuth();
  const sessionUserId = useSessionUserId();
  const [windows, setWindows] = useState<MaintenanceWindow[]>([]);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterSlaId, setFilterSlaId] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [selected, setSelected] = useState<MaintenanceWindow | null>(null);

  const loadWindows = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);

    const params = new URLSearchParams();
    if (filterSlaId) params.set("slaId", filterSlaId);
    if (filterStatus) params.set("status", filterStatus);
    const query = params.toString();

    apiFetch<MaintenanceWindow[]>(`/api/maintenance-windows${query ? `?${query}` : ""}`)
      .then(setWindows)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId, filterSlaId, filterStatus]);

  useEffect(() => {
    loadWindows();
  }, [loadWindows]);

  useEffect(() => {
    if (!sessionUserId) return;
    apiFetch<Sla[]>("/api/slas")
      .then(setSlas)
      .catch(() => setSlas([]));
  }, [sessionUserId]);

  function openCreate() {
    setSelected(null);
    setModalOpen(true);
  }

  function openEdit(window: MaintenanceWindow) {
    setSelected(window);
    setModalOpen(true);
  }

  async function handleCancel(window: MaintenanceWindow) {
    if (!confirm(`Annuler la maintenance "${window.title}" ?`)) return;
    try {
      await apiFetch<MaintenanceWindow>(`/api/maintenance-windows/${window.id}/cancel`, {
        method: "PATCH",
      });
      loadWindows();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Annulation impossible");
    }
  }

  const canEdit = (status: MaintenanceWindowStatus) =>
    status === "SCHEDULED" || status === "ACTIVE";

  return (
    <>
      <Header
        title="Maintenances"
        description={
          isClient
            ? "Fenêtres de maintenance planifiées sur vos SLA (lecture seule)."
            : "Planifiez des coupures exclues du calcul de conformité SLA."
        }
        action={
          canManageMaintenance ? (
            <Button onClick={openCreate}>
              <Plus className="h-4 w-4" />
              Nouvelle maintenance
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadWindows} />}

      <Card className="mb-6">
        <CardBody className="flex flex-wrap gap-4">
          <div className="min-w-[200px] flex-1">
            <Select value={filterSlaId} onChange={(e) => setFilterSlaId(e.target.value)}>
              <option value="">Tous les SLA</option>
              {slas.map((sla) => (
                <option key={sla.id} value={sla.id}>
                  {sla.name}
                </option>
              ))}
            </Select>
          </div>
          <div className="min-w-[180px]">
            <Select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
              <option value="">Tous les statuts</option>
              <option value="SCHEDULED">Planifiée</option>
              <option value="ACTIVE">En cours</option>
              <option value="COMPLETED">Terminée</option>
              <option value="CANCELLED">Annulée</option>
            </Select>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Fenêtres de maintenance"
          description={`${windows.length} fenêtre(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="space-y-3 p-6">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-12 animate-pulse rounded-xl bg-card/60" />
              ))}
            </div>
          ) : (
            <>
              <table className="min-w-full text-sm">
                <thead className="table-head">
                  <tr>
                    <th className="px-6 py-4 font-medium">Titre</th>
                    <th className="px-6 py-4 font-medium">SLA</th>
                    <th className="px-6 py-4 font-medium">Service</th>
                    <th className="px-6 py-4 font-medium">Début</th>
                    <th className="px-6 py-4 font-medium">Fin</th>
                    <th className="px-6 py-4 font-medium">Statut</th>
                    {canManageMaintenance && <th className="px-6 py-4 font-medium">Actions</th>}
                  </tr>
                </thead>
                <tbody>
                  {windows.map((window) => (
                    <tr key={window.id} className="table-row">
                      <td className="px-6 py-4">
                        <p className="font-medium text-heading">{window.title}</p>
                        {window.reason && (
                          <p className="mt-1 text-xs text-muted line-clamp-1">{window.reason}</p>
                        )}
                      </td>
                      <td className="px-6 py-4 text-body">{window.slaName ?? `#${window.slaId}`}</td>
                      <td className="px-6 py-4 text-muted">
                        {window.serviceName ?? "Tous"}
                      </td>
                      <td className="px-6 py-4 text-body">{formatDate(window.startTime)}</td>
                      <td className="px-6 py-4 text-body">{formatDate(window.endTime)}</td>
                      <td className="px-6 py-4">
                        <MaintenanceStatusBadge status={window.status} />
                      </td>
                      {canManageMaintenance && (
                        <td className="whitespace-nowrap px-6 py-4">
                          <div className="inline-flex items-center gap-1.5">
                            {canEdit(window.status) && (
                              <>
                                <Button
                                  variant="secondary"
                                  className="!px-2.5 !py-2"
                                  title="Modifier"
                                  onClick={() => openEdit(window)}
                                >
                                  <Pencil className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="danger"
                                  className="!px-2.5 !py-2"
                                  title="Annuler"
                                  onClick={() => handleCancel(window)}
                                >
                                  <XCircle className="h-4 w-4" />
                                </Button>
                              </>
                            )}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
              {windows.length === 0 && (
                <EmptyState
                  icon={CalendarClock}
                  title="Aucune fenêtre de maintenance"
                  description={
                    canManageMaintenance
                      ? "Planifiez une coupure pour qu'elle n'impacte pas le score SLA."
                      : "Aucune maintenance planifiée sur votre périmètre."
                  }
                />
              )}
            </>
          )}
        </CardBody>
      </Card>

      {canManageMaintenance && (
        <MaintenanceWindowFormModal
          open={modalOpen}
          onClose={() => {
            setModalOpen(false);
            setSelected(null);
          }}
          onSaved={loadWindows}
          window={selected}
        />
      )}
    </>
  );
}
