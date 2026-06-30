"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Activity, Lock, Mail } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { ApiError } from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

const DEMO_ACCOUNTS = [
  { role: "Administrateur", email: "admin@sla.com", password: "Admin123!" },
  { role: "Opérateur", email: "user@sla.com", password: "User123!" },
  { role: "Client Acme", email: "client@acme.com", password: "Client123!" },
] as const;

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
    <div className="grid min-h-screen lg:grid-cols-2 bg-gradient-to-br from-background to-background">
      {/* Left side - Hero */}
      <div className="relative hidden overflow-hidden lg:flex lg:flex-col lg:justify-between lg:p-12 bg-gradient-to-br from-slate-900 via-slate-950 to-slate-900">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(14,165,233,0.15),_transparent_50%),radial-gradient(circle_at_bottom_right,_rgba(6,182,212,0.1),_transparent_50%)]" />
        <div className="relative space-y-8 z-10">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg shadow-primary/30">
              <Activity className="h-6 w-6 text-white" />
            </div>
            <div>
              <p className="text-sm font-bold text-primary">SLA Monitoring</p>
              <p className="text-xs text-muted">Plateforme d&apos;Accord de Niveau de Service</p>
            </div>
          </div>
          <div>
            <h1 className="max-w-md text-5xl font-bold tracking-tight text-white leading-tight">
              Pilotez vos SLA avec clarté
            </h1>
            <p className="mt-6 max-w-lg text-base leading-relaxed text-muted">
              Surveillez la disponibilité en temps réel, détectez les dérives et exportez vos rapports d&apos;un seul clic. Toute votre conformité SLA au même endroit.
            </p>
          </div>
        </div>

        <div className="relative grid grid-cols-3 gap-4 z-10">
          {[
            { label: "Disponibilité", value: "99.9%" },
            { label: "Alertes", value: "Instantanées" },
            { label: "Rapports", value: "Automatisés" },
          ].map((item) => (
            <div
              key={item.label}
              className="rounded-lg border border-primary/20 bg-primary/5 backdrop-blur p-4 hover:border-primary/40 transition-colors"
            >
              <p className="text-xs uppercase tracking-wider text-muted font-semibold">
                {item.label}
              </p>
              <p className="mt-2 text-lg font-bold text-primary">{item.value}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Right side - Form */}
      <div className="flex items-center justify-center px-6 py-12 bg-gradient-to-b from-background to-background/50">
        <div className="w-full max-w-md space-y-8">
          {/* Mobile header */}
          <div className="mb-8 lg:hidden animate-fade-in">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-accent shadow-lg">
                <Activity className="h-6 w-6 text-white" />
              </div>
              <div>
                <p className="font-bold text-foreground">SLA Monitoring</p>
                <p className="text-xs text-muted">Connexion sécurisée</p>
              </div>
            </div>
          </div>

          {/* Login card */}
          <div className="surface-card p-8 animate-fade-in">
            <div className="space-y-2 mb-8">
              <h2 className="text-3xl font-bold text-foreground">Connexion</h2>
              <p className="text-sm text-muted">
                Accédez à votre tableau de bord SLA
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Email field */}
              <div className="space-y-2">
                <label className="text-sm font-semibold text-foreground">Email</label>
                <div className="relative">
                  <Mail className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
                  <Input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="pl-11"
                    placeholder="admin@sla.com"
                    required
                  />
                </div>
              </div>

              {/* Password field */}
              <div className="space-y-2">
                <label className="text-sm font-semibold text-foreground">
                  Mot de passe
                </label>
                <div className="relative">
                  <Lock className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
                  <Input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-11"
                    placeholder="••••••••"
                    required
                  />
                </div>
              </div>

              {/* Error message */}
              {error && (
                <div className="rounded-lg border border-error/50 bg-error/10 px-4 py-3 text-sm text-error font-medium animate-slide-in">
                  {error}
                </div>
              )}

              {/* Submit button */}
              <Button type="submit" className="w-full" loading={loading}>
                Se connecter
              </Button>
            </form>

            {/* Demo credentials */}
            <div className="mt-6 space-y-3 rounded-lg border border-border bg-card/50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wider text-muted">
                Comptes de démonstration
              </p>
              <div className="grid gap-2">
                {DEMO_ACCOUNTS.map((account) => (
                  <button
                    key={account.email}
                    type="button"
                    onClick={() => {
                      setEmail(account.email);
                      setPassword(account.password);
                    }}
                    className="flex items-center justify-between rounded-lg border border-border/60 bg-background/50 px-3 py-2 text-left text-sm transition hover:border-primary/40 hover:bg-primary/5"
                  >
                    <span className="font-medium text-heading">{account.role}</span>
                    <span className="font-mono text-xs text-muted">{account.email}</span>
                  </button>
                ))}
              </div>
              <p className="text-xs text-muted">
                Cliquez sur un compte pour remplir le formulaire, puis connectez-vous.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
