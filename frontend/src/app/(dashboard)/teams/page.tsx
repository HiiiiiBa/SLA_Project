"use client";

import { useCallback, useEffect, useState } from "react";
import { Pencil, Plus, Trash2, Users } from "lucide-react";
import { RequestApprovalButton } from "@/components/approval/RequestApprovalButton";
import { TeamFormModal } from "@/components/forms/TeamFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import type { Team } from "@/types";

export default function TeamsPage() {
  const { canManageOrg, isAdmin, canRequestApproval, isEmployee } = useAuth();
  const [success, setSuccess] = useState<string | null>(null);
  const sessionUserId = useSessionUserId();
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);

  const loadTeams = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    apiFetch<Team[]>("/api/teams")
      .then(setTeams)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId]);

  useEffect(() => {
    loadTeams();
  }, [loadTeams]);

  async function handleDelete(team: Team) {
    if (!confirm(`Supprimer l'équipe "${team.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/teams/${team.id}`, { method: "DELETE" });
      loadTeams();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Équipes"
        description={
          isEmployee
            ? "Équipes dont vous faites partie."
            : "Teams gérées par un manager et composées d'employés (ex. Team Dev, Team Réseaux)."
        }
        action={
          canManageOrg ? (
            <Button
              onClick={() => {
                setSelectedTeam(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouvelle équipe
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadTeams} />}
      {success && (
        <div className="mb-4 rounded-xl border border-success/30 bg-success/10 px-4 py-3 text-sm text-success">
          {success}
        </div>
      )}

      <Card>
        <CardHeader title="Liste des équipes" description={`${teams.length} équipe(s)`} />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : teams.length === 0 ? (
            <EmptyState
              icon={Users}
              title="Aucune équipe"
              description="Créez une équipe avec un manager et des employés."
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Manager</th>
                  <th className="px-6 py-4 font-medium">Employés</th>
                  <th className="px-6 py-4 font-medium">Projets</th>
                  {canManageOrg && <th className="px-6 py-4 font-medium">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {teams.map((team) => (
                  <tr key={team.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{team.name}</td>
                    <td className="px-6 py-4 text-body">{team.managerName}</td>
                    <td className="px-6 py-4 text-body">{team.memberCount}</td>
                    <td className="px-6 py-4 text-body">{team.projectCount}</td>
                    {canManageOrg && (
                      <td className="whitespace-nowrap px-6 py-4">
                        <div className="inline-flex items-center gap-1.5">
                          <Button
                            variant="secondary"
                            className="!px-2.5 !py-2"
                            onClick={() => {
                              setSelectedTeam(team);
                              setModalOpen(true);
                            }}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          {isAdmin && (
                            <Button
                              variant="danger"
                              className="!px-2.5 !py-2"
                              onClick={() => handleDelete(team)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          )}
                          {canRequestApproval && (
                            <RequestApprovalButton
                              actionType="DELETE_TEAM"
                              targetType="TEAM"
                              targetId={team.id}
                              targetLabel={team.name}
                              confirmMessage={`Demander la suppression de l'équipe "${team.name}" à l'admin ?`}
                              title="Demander la suppression (validation admin)"
                              className="!px-2.5 !py-2"
                              onSuccess={() => {
                                setSuccess(`Demande envoyée pour l'équipe "${team.name}".`);
                                setError(null);
                              }}
                              onError={setError}
                            />
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>

      {canManageOrg && (
        <TeamFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadTeams}
          team={selectedTeam}
        />
      )}
    </>
  );
}
