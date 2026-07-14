"use client";

import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getStoredAuth } from "@/lib/auth-storage";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import type { AlertNotification } from "@/types";

function resolveWsUrl(): string {
  if (process.env.NEXT_PUBLIC_WS_URL) return process.env.NEXT_PUBLIC_WS_URL;
  if (typeof window !== "undefined") return `${window.location.origin}/ws`;
  return "http://localhost:8080/ws";
}

interface UseAlertsWebSocketOptions {
  onConnectionChange?: (connected: boolean) => void;
}

export function useAlertsWebSocket(
  onAlert: (alert: AlertNotification) => void,
  options?: UseAlertsWebSocketOptions,
) {
  const callbackRef = useRef(onAlert);
  callbackRef.current = onAlert;
  const connectionCallbackRef = useRef(options?.onConnectionChange);
  connectionCallbackRef.current = options?.onConnectionChange;
  const sessionUserId = useSessionUserId();

  useEffect(() => {
    const auth = getStoredAuth();
    if (!auth?.accessToken || !sessionUserId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(resolveWsUrl()),
      connectHeaders: {
        Authorization: `Bearer ${auth.accessToken}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        connectionCallbackRef.current?.(true);
        client.subscribe("/user/queue/alerts", (message) => {
          try {
            const payload = JSON.parse(message.body) as AlertNotification;
            callbackRef.current(payload);
          } catch {
            /* ignore malformed messages */
          }
        });
      },
      onDisconnect: () => connectionCallbackRef.current?.(false),
      onStompError: () => connectionCallbackRef.current?.(false),
      onWebSocketClose: () => connectionCallbackRef.current?.(false),
    });

    client.activate();

    return () => {
      connectionCallbackRef.current?.(false);
      client.deactivate();
    };
  }, [sessionUserId]);
}
