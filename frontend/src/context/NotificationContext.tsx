"use client";

import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from "react";
import { useAppWebSocket } from "@/hooks/useAppWebSocket";
import type { AlertNotification, ApprovalNotification, LiveNotificationItem } from "@/types";

interface NotificationContextValue {
  connected: boolean;
  liveNotifications: LiveNotificationItem[];
  clearLive: () => void;
}

const NotificationContext = createContext<NotificationContextValue | null>(null);

export function NotificationProvider({ children }: { children: ReactNode }) {
  const [connected, setConnected] = useState(false);
  const [liveNotifications, setLiveNotifications] = useState<LiveNotificationItem[]>([]);

  const pushNotification = useCallback((item: LiveNotificationItem) => {
    setLiveNotifications((current) => [item, ...current].slice(0, 20));
  }, []);

  const handleAlert = useCallback(
    (notification: AlertNotification) => {
      pushNotification({
        id: `alert-${notification.alertId}-${notification.createdAt}`,
        source: "alert",
        data: notification,
      });
    },
    [pushNotification],
  );

  const handleApproval = useCallback(
    (notification: ApprovalNotification) => {
      pushNotification({
        id: `approval-${notification.requestId}-${notification.kind}-${notification.createdAt}`,
        source: "approval",
        data: notification,
      });
    },
    [pushNotification],
  );

  useAppWebSocket(handleAlert, handleApproval, { onConnectionChange: setConnected });

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
