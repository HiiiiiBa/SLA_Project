"use client";

import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getStoredAuth } from "@/lib/auth-storage";
import type { AlertNotification } from "@/types";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "http://localhost:8080/ws";

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

  useEffect(() => {
    const auth = getStoredAuth();
    if (!auth?.accessToken) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${auth.accessToken}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        connectionCallbackRef.current?.(true);
        client.subscribe("/topic/alerts", (message) => {
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
  }, []);
}
