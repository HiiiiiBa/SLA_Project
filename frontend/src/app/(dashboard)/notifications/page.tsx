"use client";

import { useCallback, useEffect, useState } from "react";
import { Bell, Mail, Radio, Wifi, WifiOff } from "lucide-react";
import { NotificationStatusBadges } from "@/components/layout/NotificationBell";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useNotifications } from "@/context/NotificationContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { NotificationChannel, NotificationRecord } from "@/types";

const channels: NotificationChannel[] = ["WEBSOCKET", "EMAIL"];

export default function NotificationsPage() {
  const { connected, liveNotifications, clearLive } = useNotifications();
  const sessionUserId = useSessionUserId();
  const [history, setHistory] = useState<NotificationRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterChannel, setFilterChannel] = useState("");

  const loadHistory = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    const query = filterChannel ? `?channel=${filterChannel}` : "";
    apiFetch<NotificationRecord[]>(`/api/notifications${query}`)
      .then(setHistory)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId, filterChannel]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  return (
    <>
      <Header
        title="Notifications"
        description="WebSocket temps réel, envoi email et historique des notifications."
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

      {error && <ErrorBanner message={error} onRetry={loadHistory} />}

      <Card className="mb-6">
        <CardHeader title="Canaux de notification" />
        <CardBody className="space-y-4">
          <NotificationStatusBadges />
          <ul className="grid gap-2 text-sm text-body sm:grid-cols-3">
            <li>Notifications temps réel via WebSocket (STOMP /topic/alerts)</li>
            <li>Notifications email aux clients et administrateurs</li>
            <li>Historique persistant de chaque envoi (SENT / FAILED)</li>
          </ul>
        </CardBody>
      </Card>

      {liveNotifications.length > 0 && (
        <Card className="mb-6">
          <CardHeader
            title="Notifications temps réel"
            description={`${liveNotifications.length} notification(s) reçue(s) via WebSocket`}
            action={
              <Button variant="secondary" onClick={clearLive}>
                Effacer
              </Button>
            }
          />
          <CardBody className="space-y-3">
            {liveNotifications.map((item) => (
              <div
                key={`live-${item.alertId}-${item.createdAt}`}
                className="animate-slide-in rounded-xl border border-success/25 bg-success/10 px-5 py-4"
              >
                <div className="flex items-start gap-3">
                  <Radio className="mt-0.5 h-4 w-4 text-success" />
                  <div>
                    <p className="font-semibold text-heading">{item.slaName}</p>
                    <p className="mt-1 text-sm text-body">{item.message}</p>
                    <p className="mt-2 text-xs text-muted">
                      {item.clientName} · {formatDate(item.createdAt)}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </CardBody>
        </Card>
      )}

      <Card className="mb-6">
        <CardHeader title="Filtres" description="Filtrer l'historique par canal" />
        <CardBody>
          <div className="max-w-xs space-y-2">
            <label className="text-xs font-semibold uppercase tracking-wider text-muted">
              Canal
            </label>
            <Select
              value={filterChannel}
              onChange={(e) => setFilterChannel(e.target.value)}
            >
              <option value="">Tous les canaux</option>
              {channels.map((channel) => (
                <option key={channel} value={channel}>
                  {channel}
                </option>
              ))}
            </Select>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Historique des notifications"
          description={`${history.length} entrée(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : history.length === 0 ? (
            <EmptyState
              icon={Bell}
              title="Aucune notification enregistrée"
              description="L'historique se remplit lors de la création d'alertes (WebSocket + email)."
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Date</th>
                  <th className="px-6 py-4 font-medium">Canal</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Destinataire</th>
                  <th className="px-6 py-4 font-medium">Message</th>
                </tr>
              </thead>
              <tbody>
                {history.map((item) => (
                  <tr key={item.id} className="table-row">
                    <td className="px-6 py-4 text-muted">{formatDate(item.createdAt)}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center gap-1.5 text-body">
                        {item.channel === "WEBSOCKET" ? (
                          <Radio className="h-3.5 w-3.5 text-primary" />
                        ) : (
                          <Mail className="h-3.5 w-3.5 text-accent" />
                        )}
                        {item.channel}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={
                          item.status === "SENT"
                            ? "font-medium text-success"
                            : "font-medium text-error"
                        }
                      >
                        {item.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-body">{item.slaName}</td>
                    <td className="max-w-[12rem] truncate px-6 py-4 text-muted">
                      {item.recipient ?? "—"}
                    </td>
                    <td className="max-w-sm px-6 py-4 text-body">{item.message}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>
    </>
  );
}
