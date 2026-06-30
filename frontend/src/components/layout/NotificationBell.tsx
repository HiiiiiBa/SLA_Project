"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Bell, Mail, Radio } from "lucide-react";
import { useNotifications } from "@/context/NotificationContext";
import { cn, formatDate } from "@/lib/utils";

export function NotificationBell() {
  const { connected, liveNotifications, clearLive } = useNotifications();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const unreadCount = liveNotifications.length;

  const close = useCallback(() => setOpen(false), []);

  useEffect(() => {
    if (!open) return;

    function handleClickOutside(event: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        close();
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open, close]);

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        className={cn(
          "relative flex h-10 w-10 items-center justify-center rounded-xl border transition-all duration-200",
          open
            ? "border-primary/40 bg-primary/10 text-primary"
            : "border-border bg-card/60 text-muted hover:border-primary/30 hover:bg-card hover:text-foreground",
        )}
        aria-label="Notifications"
        aria-expanded={open}
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-error px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
        <span
          className={cn(
            "absolute bottom-1 right-1 h-2 w-2 rounded-full ring-2 ring-card",
            connected ? "bg-success" : "bg-muted",
          )}
          title={connected ? "WebSocket connecté" : "WebSocket déconnecté"}
        />
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-2xl border border-border bg-card shadow-xl sm:w-96">
          <div className="flex items-center justify-between border-b border-border px-4 py-3">
            <div>
              <p className="text-sm font-semibold text-heading">Notifications</p>
              <p className="text-xs text-muted">
                {connected ? "Temps réel actif" : "Hors ligne"}
              </p>
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={clearLive}
                className="text-xs font-medium text-primary hover:underline"
              >
                Tout effacer
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto">
            {liveNotifications.length === 0 ? (
              <div className="px-4 py-8 text-center">
                <Bell className="mx-auto h-8 w-8 text-muted/50" />
                <p className="mt-3 text-sm text-muted">Aucune notification récente</p>
              </div>
            ) : (
              liveNotifications.map((item) => (
                <div
                  key={`${item.alertId}-${item.createdAt}`}
                  className="border-b border-border/60 px-4 py-3 last:border-b-0 hover:bg-card/80"
                >
                  <div className="flex items-start gap-2">
                    <Radio className="mt-0.5 h-3.5 w-3.5 shrink-0 text-primary" />
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-heading">
                        {item.slaName}
                      </p>
                      <p className="mt-1 line-clamp-2 text-xs text-body">{item.message}</p>
                      <p className="mt-1.5 text-[11px] text-muted">
                        {item.clientName} · {formatDate(item.createdAt)}
                      </p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="border-t border-border bg-card/80 px-4 py-3">
            <Link
              href="/notifications"
              onClick={close}
              className="block rounded-lg bg-primary/10 px-3 py-2 text-center text-xs font-semibold text-primary transition-colors hover:bg-primary/15"
            >
              Voir l&apos;historique complet
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

export function NotificationStatusBadges() {
  return (
    <div className="flex flex-wrap gap-2 text-xs text-muted">
      <span className="inline-flex items-center gap-1 rounded-full border border-border px-2 py-1">
        <Radio className="h-3 w-3 text-primary" />
        WebSocket temps réel
      </span>
      <span className="inline-flex items-center gap-1 rounded-full border border-border px-2 py-1">
        <Mail className="h-3 w-3 text-accent" />
        Email automatique
      </span>
    </div>
  );
}
