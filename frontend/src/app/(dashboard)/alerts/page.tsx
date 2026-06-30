"use client";

import { useCallback, useEffect, useState } from "react";
import { Bell, Wifi, WifiOff } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAlertsWebSocket } from "@/hooks/useAlertsWebSocket";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Alert, AlertNotification } from "@/types";

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [liveAlerts, setLiveAlerts] = useState<AlertNotification[]>([]);
  const [connected, setConnected] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Alert[]>("/api/alerts")
      .then(setAlerts)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  const handleLiveAlert = useCallback((notification: AlertNotification) => {
    setLiveAlerts((current) => [notification, ...current].slice(0, 8));
    setConnected(true);
  }, []);

  useAlertsWebSocket(handleLiveAlert);

  return (
    <>
      <Header
        title="Alertes"
        description="Historique des alertes SLA et notifications temps réel via WebSocket."
        action={
          <div className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-600 shadow-sm">
            {connected ? (
              <Wifi className="h-4 w-4 text-emerald-500" />
            ) : (
              <WifiOff className="h-4 w-4 text-red-500" />
            )}
            WebSocket {connected ? "connecté" : "déconnecté"}
          </div>
        }
      />

      {error && <ErrorBanner message={error} />}

      {liveAlerts.length > 0 && (
        <div className="mb-6 space-y-3">
          {liveAlerts.map((alert) => (
            <div
              key={`${alert.alertId}-${alert.createdAt}`}
              className="animate-slide-in rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 shadow-sm"
            >
              <div className="flex items-start gap-3">
                <div className="mt-0.5 flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-500 text-white">
                  <Bell className="h-4 w-4" />
                </div>
                <div>
                  <p className="font-medium text-emerald-900">
                    Nouvelle alerte — {alert.slaName}
                  </p>
                  <p className="mt-1 text-sm text-emerald-800">{alert.message}</p>
                  <p className="mt-2 text-xs text-emerald-700/80">
                    {alert.clientName} · {formatDate(alert.createdAt)}
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Card>
        <CardHeader
          title="Historique des alertes"
          description={`${alerts.length} alerte(s) enregistrée(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-slate-400">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-slate-500">
                <tr>
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">Type</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Message</th>
                  <th className="px-6 py-4 font-medium">Créée le</th>
                </tr>
              </thead>
              <tbody>
                {alerts.map((alert) => (
                  <tr key={alert.id} className="border-t border-slate-100 hover:bg-slate-50/70">
                    <td className="px-6 py-4 text-slate-500">#{alert.id}</td>
                    <td className="px-6 py-4 text-slate-600">{alert.type}</td>
                    <td className="px-6 py-4">
                      <StatusBadge status={alert.status} kind="alert" />
                    </td>
                    <td className="px-6 py-4 text-slate-600">#{alert.slaId}</td>
                    <td className="max-w-md px-6 py-4 text-slate-700">
                      {alert.message}
                    </td>
                    <td className="px-6 py-4 text-slate-500">
                      {formatDate(alert.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && alerts.length === 0 && (
            <div className="px-6 py-10 text-sm text-slate-400">
              Aucune alerte pour le moment.
            </div>
          )}
        </CardBody>
      </Card>
    </>
  );
}
