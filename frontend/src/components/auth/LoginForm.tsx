"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Activity, Lock, Mail } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { ApiError } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

export function LoginForm() {
  const { login, user } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("admin@sla.com");
  const [password, setPassword] = useState("Admin123!");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      router.replace("/dashboard");
    }
  }, [user, router]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login({ email, password });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Connexion impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden overflow-hidden bg-slate-950 lg:flex lg:flex-col lg:justify-between lg:p-12">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(16,185,129,0.25),_transparent_45%),radial-gradient(circle_at_bottom_right,_rgba(59,130,246,0.18),_transparent_40%)]" />
        <div className="relative">
          <div className="mb-8 flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-500/15 ring-1 ring-emerald-400/30">
              <Activity className="h-6 w-6 text-emerald-400" />
            </div>
            <div>
              <p className="text-sm font-medium text-emerald-300">SLA Monitoring</p>
              <p className="text-xs text-slate-400">Service Level Agreement Platform</p>
            </div>
          </div>
          <h1 className="max-w-md text-4xl font-semibold tracking-tight text-white">
            Pilotez vos SLA avec clarté et réactivité
          </h1>
          <p className="mt-4 max-w-lg text-base leading-relaxed text-slate-400">
            Surveillez la disponibilité, détectez les dérives en temps réel et
            exportez vos rapports PDF/CSV en un clic.
          </p>
        </div>
        <div className="relative grid grid-cols-3 gap-4">
          {[
            { label: "Uptime", value: "99.9%" },
            { label: "Alertes", value: "Temps réel" },
            { label: "Rapports", value: "PDF / CSV" },
          ].map((item) => (
            <div
              key={item.label}
              className="rounded-2xl border border-white/10 bg-white/5 p-4 backdrop-blur"
            >
              <p className="text-xs uppercase tracking-wider text-slate-400">
                {item.label}
              </p>
              <p className="mt-2 text-lg font-semibold text-white">{item.value}</p>
            </div>
          ))}
        </div>
      </div>

      <div className="flex items-center justify-center bg-slate-50 px-6 py-12 dark:bg-slate-950">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-500 text-white">
                <Activity className="h-5 w-5" />
              </div>
              <div>
                <p className="font-semibold text-heading">SLA Monitoring</p>
                <p className="text-sm text-muted">Connexion sécurisée</p>
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60 dark:border-slate-800 dark:bg-slate-900 dark:shadow-slate-950/50">
            <h2 className="text-2xl font-semibold text-heading">Connexion</h2>
            <p className="mt-2 text-sm text-muted">
              Accédez à votre tableau de bord SLA
            </p>

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
              <div className="space-y-2">
                <label className="text-sm font-medium text-body">Email</label>
                <div className="relative">
                  <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <Input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="pl-10"
                    placeholder="admin@sla.com"
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-body">
                  Mot de passe
                </label>
                <div className="relative">
                  <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                  <Input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-10"
                    placeholder="••••••••"
                    required
                  />
                </div>
              </div>

              {error && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              <Button type="submit" className="w-full" loading={loading}>
                Se connecter
              </Button>
            </form>

            <p className="mt-6 text-center text-xs text-slate-400">
              Compte démo : admin@sla.com / Admin123!
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
