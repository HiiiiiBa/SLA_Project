"use client";

import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from "react";
import { useAlertsWebSocket } from "@/hooks/useAlertsWebSocket";
import type { AlertNotification } from "@/types";

interface NotificationContextValue {
  connected: boolean;
  liveNotifications: AlertNotification[];
  clearLive: () => void;
}

const NotificationContext = createContext<NotificationContextValue | null>(null);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [connected, setConnected] = useState(false);
  const [liveNotifications, setLiveNotifications] = useState<AlertNotification[]>([]);

  const handleAlert = useCallback((notification: AlertNotification) => {
    setLiveNotifications((current) => [notification, ...current].slice(0, 12));
  }, []);

  useAlertsWebSocket(handleAlert, { onConnectionChange: setConnected });

  const clearLive = useCallback(() => setLiveNotifications([]), []);

  return (
    <NotificationContext.Provider value={{ connected, liveNotifications, clearLive }}>
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error("useNotifications must be used within NotificationProvider");
  }
  return context;
}
