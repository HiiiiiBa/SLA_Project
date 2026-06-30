"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Activity,
  AlertTriangle,
  Building2,
  FileText,
  Gauge,
  LayoutDashboard,
  LogOut,
  Server,
  Settings,
  Siren,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/dashboard", label: "Tableau de bord", icon: LayoutDashboard },
  { href: "/slas", label: "SLA", icon: Gauge },
  { href: "/services", label: "Services", icon: Server },
  { href: "/incidents", label: "Incidents", icon: Siren },
  { href: "/alerts", label: "Alertes", icon: AlertTriangle },
  { href: "/reports", label: "Rapports", icon: FileText },
  { href: "/clients", label: "Clients", icon: Building2, adminOnly: true },
  { href: "/admin", label: "Administration", icon: Settings, adminOnly: true },
];

export function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const { user, logout, isAdmin } = useAuth();

  return (
    <aside className="fixed inset-y-0 left-0 z-30 flex w-72 flex-col border-r border-border bg-card/95 backdrop-blur-xl text-foreground lg:block">
      {/* Logo section */}
      <div className="flex items-center gap-3 border-b border-border/50 px-6 py-8">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg">
          <Activity className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="font-bold text-foreground">SLA Monitor</p>
          <p className="text-xs text-muted">Gestion intelligente</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 px-3 py-8 overflow-y-auto">
        {navItems
          .filter((item) => !item.adminOnly || isAdmin)
          .map((item) => {
            const Icon = item.icon;
            const active = pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={onNavigate}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-all duration-200",
                  active
                    ? "bg-gradient-to-r from-primary/20 to-accent/10 text-primary border border-primary/30 shadow-md"
                    : "text-muted hover:text-foreground hover:bg-card/50",
                )}
              >
                <Icon className="h-4 w-4 flex-shrink-0" />
                <span>{item.label}</span>
                {active && <div className="ml-auto h-2 w-2 rounded-full bg-primary" />}
              </Link>
            );
          })}
      </nav>

      {/* User section */}
      <div className="border-t border-border/50 p-4 space-y-4">
        <div className="rounded-lg bg-gradient-to-br from-primary/5 to-accent/5 border border-primary/20 p-4">
          <p className="truncate text-sm font-medium text-foreground">{user?.email}</p>
          <p className="mt-2 text-xs uppercase tracking-wider text-primary font-semibold">
            {user?.role}
          </p>
        </div>
        
        <button
          onClick={() => logout()}
          className="flex w-full items-center justify-center gap-2 rounded-lg border border-border bg-card/50 px-3 py-2 text-sm font-medium text-foreground transition-all duration-200 hover:bg-error/10 hover:border-error/50 hover:text-error"
        >
          <LogOut className="h-4 w-4" />
          Déconnexion
        </button>
      </div>
    </aside>
  );
}
