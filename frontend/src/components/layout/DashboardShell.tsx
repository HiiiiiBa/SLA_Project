"use client";

import { useState } from "react";
import { Activity, Menu, Moon, Sun, X } from "lucide-react";
import { AuthGuard } from "@/components/auth/AuthGuard";
import { Sidebar } from "@/components/layout/Sidebar";
import { useTheme } from "@/context/ThemeContext";

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { theme, toggleTheme } = useTheme();

  return (
    <AuthGuard>
      <div className="page-shell">
        <div className="sticky top-0 z-40 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 dark:border-slate-800 dark:bg-slate-900 lg:hidden">
          <div className="flex items-center gap-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-500 text-white">
              <Activity className="h-4 w-4" />
            </div>
            <span className="font-semibold text-heading">SLA Monitor</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={toggleTheme}
              className="rounded-xl border border-slate-200 p-2 text-slate-600 dark:border-slate-700 dark:text-slate-300"
            >
              {theme === "dark" ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>
            <button
              onClick={() => setMobileOpen((open) => !open)}
              className="rounded-xl border border-slate-200 p-2 text-slate-600 dark:border-slate-700 dark:text-slate-300"
              aria-label="Menu"
            >
              {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        <div className={`${mobileOpen ? "block" : "hidden"} lg:block`}>
          <Sidebar onNavigate={() => setMobileOpen(false)} />
        </div>

        <main className="lg:pl-72">
          <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
            {children}
          </div>
        </main>
      </div>
    </AuthGuard>
  );
}
