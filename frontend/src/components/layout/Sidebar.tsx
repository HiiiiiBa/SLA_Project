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
import { ThemeToggle } from "@/components/ui/ThemeToggle";
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
    <aside className="fixed inset-y-0 left-0 z-30 flex w-72 flex-col border-r border-slate-800 bg-slate-950 text-slate-300 lg:block">
      <div className="flex items-center gap-3 border-b border-slate-800 px-6 py-6">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500/15 ring-1 ring-emerald-400/30">
          <Activity className="h-5 w-5 text-emerald-400" />
        </div>
        <div>
          <p className="font-semibold text-white">SLA Monitor</p>
          <p className="text-xs text-slate-500">Gestion intelligente</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-4 py-6">
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
                  "flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition",
                  active
                    ? "bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/20"
                    : "text-slate-400 hover:bg-slate-900 hover:text-white",
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
      </nav>

      <div className="border-t border-slate-800 p-4">
        <div className="rounded-2xl bg-slate-900/80 p-4">
          <p className="truncate text-sm font-medium text-white">{user?.email}</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-emerald-400">
            {user?.role}
          </p>
          <div className="mt-4">
            <ThemeToggle />
          </div>
          <button
            onClick={() => logout()}
            className="mt-3 flex w-full items-center justify-center gap-2 rounded-xl border border-slate-700 px-3 py-2 text-sm text-slate-300 transition hover:bg-slate-800 hover:text-white"
          >
            <LogOut className="h-4 w-4" />
            Déconnexion
          </button>
        </div>
      </div>
    </aside>
  );
}
