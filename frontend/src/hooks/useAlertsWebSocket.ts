"use client";

import { useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getStoredAuth } from "@/lib/auth-storage";
import type { AlertNotification } from "@/types";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "http://localhost:8080/ws";

export function useAlertsWebSocket(
  onAlert: (alert: AlertNotification) => void,
) {
  const callbackRef = useRef(onAlert);
  callbackRef.current = onAlert;

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
        client.subscribe("/topic/alerts", (message) => {
          try {
            const payload = JSON.parse(message.body) as AlertNotification;
            callbackRef.current(payload);
          } catch {
            /* ignore malformed messages */
          }
        });
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);
}
