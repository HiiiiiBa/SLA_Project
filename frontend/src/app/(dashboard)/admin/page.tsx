"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Cpu, KeyRound, Pencil, Plus, Power, PowerOff, Trash2, Zap } from "lucide-react";
import { UserFormModal } from "@/components/forms/UserFormModal";
import { ResetPasswordModal } from "@/components/forms/ResetPasswordModal";
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
  Sla,
  SlaEvaluation,
  User,
} from "@/types";

const scenarios: SimulationScenario[] = ["NORMAL", "DEGRADED", "OUTAGE"];

export default function AdminPage() {
  const { isAdmin } = useAuth();
  const router = useRouter();
  const [users, setUsers] = useState<User[]>([]);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [selectedSlaId, setSelectedSlaId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [scenario, setScenario] = useState<SimulationScenario>("NORMAL");
  const [engineLoading, setEngineLoading] = useState(false);
  const [singleEngineLoading, setSingleEngineLoading] = useState(false);
  const [simLoading, setSimLoading] = useState(false);
  const [lastEvaluations, setLastEvaluations] = useState<SlaEvaluation[]>([]);
  const [lastSingleEvaluation, setLastSingleEvaluation] = useState<SlaEvaluation | null>(null);
  const [lastSimulation, setLastSimulation] = useState<MetricSimulationResult | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [resetModalOpen, setResetModalOpen] = useState(false);
  const [resetUser, setResetUser] = useState<User | null>(null);

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

  const loadSlas = useCallback(() => {
    apiFetch<Sla[]>("/api/slas")
      .then(setSlas)
      .catch(() => setSlas([]));
  }, []);

  useEffect(() => {
    if (!isAdmin) {
      router.replace("/dashboard");
      return;
    }
    loadUsers();
    loadSlas();
  }, [isAdmin, router, loadUsers, loadSlas]);

  async function runSlaEvaluation() {
    setEngineLoading(true);
    setError(null);
    try {
      const results = await apiFetch<SlaEvaluation[]>("/api/admin/sla-engine/evaluate", {
        method: "POST",
      });
      setLastEvaluations(results);
      setLastSingleEvaluation(null);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Évaluation SLA échouée");
    } finally {
      setEngineLoading(false);
    }
  }

  async function runSingleSlaEvaluation() {
    if (!selectedSlaId) return;
    setSingleEngineLoading(true);
    setError(null);
    try {
      const result = await apiFetch<SlaEvaluation>(
        `/api/admin/sla-engine/evaluate/${selectedSlaId}`,
        { method: "POST" },
      );
      setLastSingleEvaluation(result);
      setLastEvaluations([]);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Évaluation SLA échouée");
    } finally {
      setSingleEngineLoading(false);
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

  async function toggleUserStatus(user: User) {
    const action = user.enabled ? "deactivate" : "activate";
    const label = user.enabled ? "désactiver" : "activer";
    if (!confirm(`${label.charAt(0).toUpperCase() + label.slice(1)} le compte ${user.email} ?`)) return;
    try {
      await apiFetch<User>(`/api/admin/users/${user.id}/${action}`, { method: "PATCH" });
      loadUsers();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Action impossible");
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
            title="Moteur SLA (SLA Engine)"
            description="Calcul manuel, automatique (scheduler) et recalcul par contrat."
          />
          <CardBody className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted">
                  Fonctionnalités
                </p>
                <ul className="space-y-1 text-sm text-body">
                  <li>Lancer calcul manuel SLA (tous)</li>
                  <li>Calcul automatique via scheduler (horaire)</li>
                  <li>Recalcul par SLA</li>
                  <li>Mise à jour statuts SLA</li>
                </ul>
              </div>
              <div>
                <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted">
                  Logique
                </p>
                <ul className="space-y-1 text-sm text-body">
                  <li>Analyse des métriques</li>
                  <li>Comparaison avec objectifs SLA</li>
                  <li>Changement automatique de statut</li>
                  <li>Génération d&apos;alertes</li>
                </ul>
              </div>
            </div>

            <Button onClick={runSlaEvaluation} loading={engineLoading}>
              <Cpu className="h-4 w-4" />
              Évaluer tous les SLA
            </Button>

            <div className="flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-end">
              <div className="flex-1 space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Recalcul par SLA
                </label>
                <Select
                  value={selectedSlaId}
                  onChange={(e) => setSelectedSlaId(e.target.value)}
                >
                  <option value="">Sélectionner un contrat</option>
                  {slas.map((sla) => (
                    <option key={sla.id} value={sla.id}>
                      {sla.name} ({sla.status})
                    </option>
                  ))}
                </Select>
              </div>
              <Button
                variant="secondary"
                onClick={runSingleSlaEvaluation}
                loading={singleEngineLoading}
                disabled={!selectedSlaId}
              >
                <Cpu className="h-4 w-4" />
                Recalculer ce SLA
              </Button>
            </div>

            {lastEvaluations.length > 0 && (
              <div className="rounded-xl border border-border bg-card/50 p-4 backdrop-blur-sm">
                <p className="text-sm font-medium text-heading">
                  {lastEvaluations.length} SLA évalué(s)
                </p>
                <ul className="mt-3 space-y-2 text-sm text-body">
                  {lastEvaluations.slice(0, 5).map((ev) => (
                    <li key={ev.slaId}>
                      <span className="font-semibold text-heading">{ev.slaName}</span> — score{" "}
                      <span className="font-semibold text-heading">{formatScore(ev.slaScore)}</span> —{" "}
                      {ev.previousStatus} → {ev.currentStatus}
                      {ev.alertCreated && " · alerte créée"}
                      {ev.reportCreated && " · rapport créé"}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {lastSingleEvaluation && (
              <div className="rounded-xl border border-border bg-card/50 p-4 backdrop-blur-sm">
                <p className="text-sm font-medium text-heading">
                  {lastSingleEvaluation.slaName}
                </p>
                <p className="mt-2 text-sm text-body">
                  Score {formatScore(lastSingleEvaluation.slaScore)} —{" "}
                  {lastSingleEvaluation.previousStatus} → {lastSingleEvaluation.currentStatus}
                  {lastSingleEvaluation.statusChanged && " (statut mis à jour)"}
                  {lastSingleEvaluation.alertCreated && " · alerte créée"}
                  {lastSingleEvaluation.reportCreated && " · rapport créé"}
                </p>
                <p className="mt-1 text-xs text-muted">
                  {lastSingleEvaluation.metricsAnalyzed} métrique(s),{" "}
                  {lastSingleEvaluation.incidentsAnalyzed} incident(s) analysé(s)
                </p>
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
              <div className="rounded-xl border border-success/25 bg-success/10 p-4">
                <p className="text-sm text-body">
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
          title="Gestion des utilisateurs"
          description={`${users.length} compte(s) — activer, désactiver ou réinitialiser le mot de passe`}
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
                    <td className="px-6 py-4">
                      <span
                        className={
                          user.enabled
                            ? "font-medium text-success"
                            : "font-medium text-muted"
                        }
                      >
                        {user.enabled ? "Actif" : "Désactivé"}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-muted">{formatDate(user.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex flex-wrap gap-2">
                        <Button
                          variant="secondary"
                          onClick={() => toggleUserStatus(user)}
                          title={user.enabled ? "Désactiver" : "Activer"}
                        >
                          {user.enabled ? (
                            <PowerOff className="h-4 w-4" />
                          ) : (
                            <Power className="h-4 w-4" />
                          )}
                        </Button>
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setResetUser(user);
                            setResetModalOpen(true);
                          }}
                          title="Réinitialiser le mot de passe"
                        >
                          <KeyRound className="h-4 w-4" />
                        </Button>
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

      <ResetPasswordModal
        open={resetModalOpen}
        onClose={() => setResetModalOpen(false)}
        onSaved={loadUsers}
        user={resetUser}
      />
    </>
  );
}
