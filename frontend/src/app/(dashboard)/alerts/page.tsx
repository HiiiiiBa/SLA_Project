"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { Bell, CheckCircle2, Eye, Trash2, Wifi, WifiOff } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { useNotifications } from "@/context/NotificationContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type {
  Alert,
  ServiceEntity,
  Sla,
} from "@/types";

export default function AlertsPage() {
  const { isAdmin } = useAuth();
  const sessionUserId = useSessionUserId();
  const { connected, liveNotifications } = useNotifications();
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterSlaId, setFilterSlaId] = useState("");
  const [filterServiceId, setFilterServiceId] = useState("");
  const [filterType, setFilterType] = useState("");
  const [filterStatus, setFilterStatus] = useState("");

  const loadAlerts = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);

    const params = new URLSearchParams();
    if (filterSlaId) params.set("slaId", filterSlaId);
    if (filterServiceId) params.set("serviceId", filterServiceId);
    if (filterType) params.set("type", filterType);
    if (filterStatus) params.set("status", filterStatus);
    const query = params.toString();

    apiFetch<Alert[]>(`/api/alerts${query ? `?${query}` : ""}`)
      .then(setAlerts)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId, filterSlaId, filterServiceId, filterType, filterStatus]);

  useEffect(() => {
    loadAlerts();
  }, [loadAlerts]);

  useEffect(() => {
    if (!sessionUserId) return;
    Promise.all([
      apiFetch<Sla[]>("/api/slas"),
      apiFetch<ServiceEntity[]>("/api/services"),
    ])
      .then(([slaData, serviceData]) => {
        setSlas(slaData);
        setServices(serviceData);
      })
      .catch(() => {
        setSlas([]);
        setServices([]);
      });
  }, [sessionUserId]);

  const filteredServices = useMemo(() => {
    if (!filterSlaId) return services;
    return services.filter((service) => String(service.slaId) === filterSlaId);
  }, [services, filterSlaId]);

  useEffect(() => {
    if (liveNotifications.length > 0) {
      loadAlerts();
    }
  }, [liveNotifications.length, loadAlerts]);

  async function handleMarkRead(alert: Alert) {
    try {
      await apiFetch<Alert>(`/api/alerts/${alert.id}/read`, { method: "PATCH" });
      loadAlerts();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Action impossible");
    }
  }

  async function handleResolve(alert: Alert) {
    try {
      await apiFetch<Alert>(`/api/alerts/${alert.id}/resolve`, { method: "PATCH" });
      loadAlerts();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Action impossible");
    }
  }

  async function handleDelete(alert: Alert) {
    if (!confirm(`Supprimer l'alerte #${alert.id} ?`)) return;
    try {
      await apiFetch<void>(`/api/alerts/${alert.id}`, { method: "DELETE" });
      loadAlerts();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Alertes"
        description="Filtrage, actions et notifications temps réel (SLA BREACHED, service DOWN, taux d'erreur élevé)."
        action={
          <div className="inline-flex items-center gap-2 rounded-full border border-border bg-card/70 px-4 py-2 text-sm text-muted shadow-sm backdrop-blur">
            {connected ? (
              <Wifi className="h-4 w-4 text-success" />
            ) : (
              <WifiOff className="h-4 w-4 text-error" />
            )}
            WebSocket {connected ? "connecté" : "déconnecté"}
          </div>
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadAlerts} />}

      {liveNotifications.length > 0 && (
        <div className="mb-6 space-y-3">
          {liveNotifications.slice(0, 5).map((alert) => (
            <div
              key={`${alert.alertId}-${alert.createdAt}`}
              className="animate-slide-in rounded-2xl border border-success/25 bg-success/10 px-5 py-4 shadow-sm"
            >
              <div className="flex items-start gap-3">
                <div className="mt-0.5 flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-accent text-white shadow-lg shadow-primary/20">
                  <Bell className="h-4 w-4" />
                </div>
                <div>
                  <p className="font-semibold text-heading">
                    Nouvelle alerte — {alert.slaName}
                  </p>
                  <p className="mt-1 text-sm text-body">{alert.message}</p>
                  <p className="mt-2 text-xs text-muted">
                    {alert.clientName} · {formatDate(alert.createdAt)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Card className="mb-6">
        <CardHeader title="Filtres" description="Affinez la liste des alertes" />
        <CardBody>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">SLA</label>
              <Select
                value={filterSlaId}
                onChange={(e) => {
                  setFilterSlaId(e.target.value);
                  setFilterServiceId("");
                }}
              >
                <option value="">Tous les SLA</option>
                {slas.map((sla) => (
                  <option key={sla.id} value={sla.id}>
                    {sla.name}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">Service</label>
              <Select
                value={filterServiceId}
                onChange={(e) => setFilterServiceId(e.target.value)}
              >
                <option value="">Tous les services</option>
                {filteredServices.map((service) => (
                  <option key={service.id} value={service.id}>
                    {service.name}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">Type</label>
              <Select value={filterType} onChange={(e) => setFilterType(e.target.value)}>
                <option value="">Tous les types</option>
                <option value="WEB">WEB</option>
                <option value="EMAIL">EMAIL</option>
              </Select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">Statut</label>
              <Select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                <option value="">Tous les statuts</option>
                <option value="NEW">NEW</option>
                <option value="READ">READ</option>
                <option value="RESOLVED">RESOLVED</option>
              </Select>
            </div>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Historique des alertes"
          description={`${alerts.length} alerte(s) affichée(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">Type</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Service</th>
                  <th className="px-6 py-4 font-medium">Message</th>
                  <th className="px-6 py-4 font-medium">Créée le</th>
                  {isAdmin && <th className="min-w-[11rem] whitespace-nowrap px-6 py-4 font-medium">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {alerts.map((alert) => (
                  <tr key={alert.id} className="table-row">
                    <td className="px-6 py-4 text-muted">#{alert.id}</td>
                    <td className="px-6 py-4 text-body">{alert.type}</td>
                    <td className="px-6 py-4">
                      <StatusBadge status={alert.status} kind="alert" />
                    </td>
                    <td className="px-6 py-4 text-body">
                      {alert.slaName ?? `#${alert.slaId}`}
                    </td>
                    <td className="px-6 py-4 text-body">
                      {alert.serviceName ?? (alert.serviceId ? `#${alert.serviceId}` : "—")}
                    </td>
                    <td className="max-w-md px-6 py-4 text-body">{alert.message}</td>
                    <td className="px-6 py-4 text-muted">{formatDate(alert.createdAt)}</td>
                    {isAdmin && (
                      <td className="whitespace-nowrap px-6 py-4">
                        <div className="inline-flex items-center gap-1.5">
                          {alert.status === "NEW" && (
                            <Button
                              variant="secondary"
                              className="!px-2.5 !py-2"
                              title="Marquer comme lu"
                              onClick={() => handleMarkRead(alert)}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                          )}
                          {alert.status !== "RESOLVED" && (
                            <Button
                              variant="secondary"
                              className="!px-2.5 !py-2"
                              title="Résoudre"
                              onClick={() => handleResolve(alert)}
                            >
                              <CheckCircle2 className="h-4 w-4" />
                            </Button>
                          )}
                          <Button
                            variant="danger"
                            className="!px-2.5 !py-2"
                            title="Supprimer"
                            onClick={() => handleDelete(alert)}
                          >
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
          {!loading && alerts.length === 0 && (
            <EmptyState
              icon={Bell}
              title="Aucune alerte pour le moment"
              description="Les alertes sont générées automatiquement en cas de SLA BREACHED, service DOWN ou taux d'erreur élevé."
            />
          )}
        </CardBody>
      </Card>
    </>
  );
}
