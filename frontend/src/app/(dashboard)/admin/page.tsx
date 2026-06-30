"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Cpu, Pencil, Plus, Trash2, Zap } from "lucide-react";
import { UserFormModal } from "@/components/forms/UserFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatScore } from "@/lib/utils";
import type {
  MetricSimulationResult,
  SimulationScenario,
  SlaEvaluation,
  User,
} from "@/types";

const scenarios: SimulationScenario[] = ["NORMAL", "DEGRADED", "OUTAGE"];

export default function AdminPage() {
  const { isAdmin } = useAuth();
  const router = useRouter();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scenario, setScenario] = useState<SimulationScenario>("NORMAL");
  const [engineLoading, setEngineLoading] = useState(false);
  const [simLoading, setSimLoading] = useState(false);
  const [lastEvaluations, setLastEvaluations] = useState<SlaEvaluation[]>([]);
  const [lastSimulation, setLastSimulation] = useState<MetricSimulationResult | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const loadUsers = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<User[]>("/api/admin/users")
      .then(setUsers)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!isAdmin) {
      router.replace("/dashboard");
      return;
    }
    loadUsers();
  }, [isAdmin, router, loadUsers]);

  async function runSlaEvaluation() {
    setEngineLoading(true);
    setError(null);
    try {
      const results = await apiFetch<SlaEvaluation[]>("/api/admin/sla-engine/evaluate", {
        method: "POST",
      });
      setLastEvaluations(results);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Évaluation SLA échouée");
    } finally {
      setEngineLoading(false);
    }
  }

  async function runSimulation() {
    setSimLoading(true);
    setError(null);
    try {
      const result = await apiFetch<MetricSimulationResult>(
        `/api/admin/metrics/simulate?scenario=${scenario}`,
        { method: "POST" },
      );
      setLastSimulation(result);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Simulation échouée");
    } finally {
      setSimLoading(false);
    }
  }

  async function handleDelete(user: User) {
    if (!confirm(`Supprimer l'utilisateur ${user.email} ?`)) return;
    try {
      await apiFetch<void>(`/api/admin/users/${user.id}`, { method: "DELETE" });
      loadUsers();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  if (!isAdmin) return null;

  return (
    <>
      <Header
        title="Administration"
        description="Moteur SLA, simulation de métriques et gestion des utilisateurs."
      />

      {error && <ErrorBanner message={error} onRetry={loadUsers} />}

      <div className="mb-8 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader
            title="Moteur SLA"
            description="Évalue tous les contrats actifs et génère alertes/rapports."
          />
          <CardBody className="space-y-4">
            <Button onClick={runSlaEvaluation} loading={engineLoading}>
              <Cpu className="h-4 w-4" />
              Évaluer tous les SLA
            </Button>
            {lastEvaluations.length > 0 && (
              <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 dark:border-slate-800 dark:bg-slate-800/50">
                <p className="text-sm font-medium text-heading">
                  {lastEvaluations.length} SLA évalué(s)
                </p>
                <ul className="mt-3 space-y-2 text-sm text-body">
                  {lastEvaluations.slice(0, 5).map((ev) => (
                    <li key={ev.slaId}>
                      {ev.slaName} — score {formatScore(ev.slaScore)} — {ev.currentStatus}
                      {ev.alertCreated && " · alerte créée"}
                      {ev.reportCreated && " · rapport créé"}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Simulation métriques"
            description="Génère des points UP/DOWN pour alimenter les graphiques."
          />
          <CardBody className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row">
              <Select
                value={scenario}
                onChange={(e) => setScenario(e.target.value as SimulationScenario)}
                className="sm:max-w-xs"
              >
                {scenarios.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </Select>
              <Button onClick={runSimulation} loading={simLoading}>
                <Zap className="h-4 w-4" />
                Simuler
              </Button>
            </div>
            {lastSimulation && (
              <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 dark:border-emerald-900 dark:bg-emerald-950/40">
                <p className="text-sm text-emerald-800 dark:text-emerald-300">
                  Scénario <strong>{lastSimulation.scenario}</strong> —{" "}
                  {lastSimulation.metricsGenerated} métrique(s) générée(s) sur{" "}
                  {lastSimulation.servicesProcessed} service(s)
                </p>
              </div>
            )}
            <p className="text-xs text-muted">
              Astuce : Simuler → Évaluer SLA → consulter le dashboard et les rapports.
            </p>
          </CardBody>
        </Card>
      </div>

      <Card>
        <CardHeader
          title="Utilisateurs"
          description={`${users.length} compte(s)`}
          action={
            <Button
              onClick={() => {
                setSelectedUser(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouvel utilisateur
            </Button>
          }
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Email</th>
                  <th className="px-6 py-4 font-medium">Rôle</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">Créé le</th>
                  <th className="px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="px-6 py-4 text-body">{user.email}</td>
                    <td className="px-6 py-4 text-body">{user.role}</td>
                    <td className="px-6 py-4 text-body">
                      {user.enabled ? "Actif" : "Désactivé"}
                    </td>
                    <td className="px-6 py-4 text-muted">{formatDate(user.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-2">
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setSelectedUser(user);
                            setModalOpen(true);
                          }}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="danger" onClick={() => handleDelete(user)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>

      <Card className="mt-6">
        <CardHeader title="Comptes démo (dev)" description="Créés automatiquement au démarrage" />
        <CardBody>
          <ul className="space-y-2 text-sm text-body">
            <li><strong>admin@sla.com</strong> / Admin123! — ADMIN</li>
            <li><strong>user@sla.com</strong> / User123! — USER</li>
            <li><strong>client@acme.com</strong> / Client123! — CLIENT</li>
          </ul>
        </CardBody>
      </Card>

      <UserFormModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSaved={loadUsers}
        user={selectedUser}
      />
    </>
  );
}
