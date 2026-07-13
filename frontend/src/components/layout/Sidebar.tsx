"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Activity,
  AlertTriangle,
  Building2,
  CalendarClock,
  ClipboardCheck,
  FileText,
  FolderKanban,
  Gauge,
  LayoutDashboard,
  LogOut,
  Settings,
  Siren,
  Users,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/dashboard", label: "Tableau de bord", icon: LayoutDashboard },
  { href: "/slas", label: "SLA", icon: Gauge },
  { href: "/maintenance", label: "Maintenances", icon: CalendarClock },
  { href: "/incidents", label: "Incidents", icon: Siren },
  { href: "/projects", label: "Projets", icon: FolderKanban },
  { href: "/teams", label: "Équipes", icon: Users, hideForClient: true, hideForEmployee: true },
  { href: "/alerts", label: "Alertes", icon: AlertTriangle },
  { href: "/reports", label: "Rapports", icon: FileText },
  { href: "/clients", label: "Clients", icon: Building2, clientsOnly: true },
  { href: "/admin/approvals", label: "Validations", icon: ClipboardCheck, adminOnly: true },
  { href: "/admin", label: "Administration", icon: Settings, adminOnly: true },
];

export function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const { user, logout, isAdmin, isClient, isEmployee, isManager, canViewClients } = useAuth();

  return (
    <aside className="fixed inset-y-0 left-0 z-30 flex h-dvh max-h-dvh w-72 flex-col overflow-hidden border-r border-border bg-card/95 backdrop-blur-xl text-foreground lg:block">
      <div className="flex shrink-0 items-center gap-3 border-b border-border/50 px-6 py-5">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg">
          <Activity className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="font-bold text-foreground">SLA Monitor</p>
          <p className="text-xs text-muted">Supervision SLA</p>
        </div>
      </div>

      <div className="scroll-area scroll-area-sidebar min-h-0 flex-1 overflow-y-auto overscroll-contain">
        <nav className="space-y-1 px-3 py-4">
          {navItems
            .filter((item) => {
              if (item.adminOnly && !isAdmin) return false;
              if ("clientsOnly" in item && item.clientsOnly && !canViewClients) return false;
              if ("hideForClient" in item && item.hideForClient && isClient) return false;
              if ("hideForEmployee" in item && item.hideForEmployee && isEmployee) return false;
              if ("hideForManager" in item && item.hideForManager && isManager) return false;
              return true;
            })
            .map((item) => {
              const Icon = item.icon;
              const active = pathname.startsWith(item.href);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onNavigate}
                  className={cn(
                    "flex items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-medium transition-all duration-200",
                    active
                      ? "border border-primary/30 bg-gradient-to-r from-primary/20 to-accent/10 text-primary shadow-md"
                      : "text-muted hover:bg-card/50 hover:text-foreground",
                  )}
                >
                  <Icon className="h-4 w-4 flex-shrink-0" />
                  <span>{item.label}</span>
                  {active && <div className="ml-auto h-2 w-2 rounded-full bg-primary" />}
                </Link>
              );
            })}
        </nav>

        <div className="mt-auto border-t border-border/50 space-y-4 p-4">
          <div className="rounded-lg border border-primary/20 bg-gradient-to-br from-primary/5 to-accent/5 p-4">
            <p className="truncate text-sm font-medium text-foreground">{user?.email}</p>
            <p className="mt-2 text-xs font-semibold uppercase tracking-wider text-primary">
              {user?.role}
            </p>
          </div>

          <button
            onClick={() => logout()}
            className="flex w-full items-center justify-center gap-2 rounded-lg border border-border bg-card/50 px-3 py-2 text-sm font-medium text-foreground transition-all duration-200 hover:border-error/50 hover:bg-error/10 hover:text-error"
          >
            <LogOut className="h-4 w-4" />
            Déconnexion
          </button>
        </div>
      </div>
    </aside>
  );
}
