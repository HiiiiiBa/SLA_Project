"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import { Activity, Menu, X } from "lucide-react";
import { AuthGuard } from "@/components/auth/AuthGuard";
import { RoleRouteGuard } from "@/components/auth/RoleRouteGuard";
import { NotificationBell } from "@/components/layout/NotificationBell";
import { Sidebar } from "@/components/layout/Sidebar";
import { ThemeToggle } from "@/components/ui/ThemeToggle";
import { useAuth } from "@/context/AuthContext";
import { NotificationProvider } from "@/context/NotificationContext";
import { FloatingChatbot } from "@/components/ai/FloatingChatbot";

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user } = useAuth();
  const sessionKey = user?.userId ?? "anonymous";

  return (
    <AuthGuard>
      <NotificationProvider key={sessionKey}>
        <div className="page-shell">
        {/* Mobile header */}
        <div className="sticky top-0 z-40 flex items-center justify-between border-b border-border bg-card/80 backdrop-blur-md px-4 py-3 lg:hidden">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent">
              <Activity className="h-5 w-5 text-white" />
            </div>
            <span className="font-bold text-heading">SLA Monitor</span>
          </div>
          <div className="flex items-center gap-2">
            <NotificationBell />
            <ThemeToggle variant="icon" />
            <button
              onClick={() => setMobileOpen((open) => !open)}
              className="rounded-lg border border-border bg-card/50 p-2 text-muted hover:text-foreground transition-colors"
              aria-label="Toggle menu"
            >
              {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {/* Sidebar */}
        <div
          className={cn(
            mobileOpen ? "fixed inset-0 z-40 lg:contents" : "hidden lg:contents",
          )}
        >
          {mobileOpen && (
            <button
              type="button"
              className="absolute inset-0 bg-black/40 lg:hidden"
              onClick={() => setMobileOpen(false)}
              aria-label="Fermer le menu"
            />
          )}
          <Sidebar onNavigate={() => setMobileOpen(false)} />
        </div>

        {/* Main content */}
        <main className="lg:pl-72">
          <div className="hidden lg:flex sticky top-0 z-20 items-center justify-end gap-2 border-b border-border/50 bg-card/50 px-8 py-3 backdrop-blur-md">
            <NotificationBell />
            <ThemeToggle variant="icon" />
          </div>
          <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8 animate-fade-in">
            <div key={sessionKey}>
              <RoleRouteGuard>{children}</RoleRouteGuard>
            </div>
          </div>
        </main>
        <FloatingChatbot />
        </div>
      </NotificationProvider>
    </AuthGuard>
  );
}
