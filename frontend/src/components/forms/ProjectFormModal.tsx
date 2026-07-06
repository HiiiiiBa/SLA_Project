"use client";

import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { ApiError, apiFetch } from "@/lib/api";
import type {
  Client,
  Project,
  ProjectCreateRequest,
  ProjectStatus,
  ProjectUpdateRequest,
  Team,
  User,
} from "@/types";

interface ProjectFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  project?: Project | null;
  defaultClientId?: number;
}

export function ProjectFormModal({
  open,
  onClose,
  onSaved,
  project,
  defaultClientId,
}: ProjectFormModalProps) {
  const isEdit = Boolean(project);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState<ProjectStatus>("ACTIVE");
  const [clientId, setClientId] = useState("");
  const [teamId, setTeamId] = useState("");
  const [memberIds, setMemberIds] = useState<number[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [teams, setTeams] = useState<Team[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const selectedTeam = useMemo(
    () => teams.find((team) => String(team.id) === teamId),
    [teams, teamId],
  );

  useEffect(() => {
    if (!open) return;
    Promise.all([
      apiFetch<Client[]>("/api/clients"),
      apiFetch<Team[]>("/api/teams"),
    ])
      .then(([clientData, teamData]) => {
        setClients(clientData);
        setTeams(teamData);
      })
      .catch(() => {
        setClients([]);
        setTeams([]);
      });
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setName(project?.name ?? "");
    setDescription(project?.description ?? "");
    setStatus(project?.status ?? "ACTIVE");
    setClientId(String(project?.clientId ?? defaultClientId ?? ""));
    setTeamId(project?.teamId ? String(project.teamId) : "");
    setMemberIds(project?.assignedMembers.map((m) => m.id) ?? []);
    setError("");
  }, [open, project, defaultClientId]);

  function toggleMember(id: number) {
    setMemberIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && project) {
        const payload: ProjectUpdateRequest = {
          name,
          description,
          status,
          clientId: Number(clientId),
          teamId: teamId ? Number(teamId) : undefined,
          memberIds,
        };
        await apiFetch<Project>(`/api/projects/${project.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: ProjectCreateRequest = {
          name,
          description,
          clientId: Number(clientId),
          teamId: teamId ? Number(teamId) : undefined,
          memberIds,
        };
        await apiFetch<Project>("/api/projects", {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur lors de l'enregistrement");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? "Modifier le projet" : "Nouveau projet"}
      description="Associez un client, une équipe et des employés."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="project-name">Nom</Label>
          <Input id="project-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div className="space-y-2">
          <Label htmlFor="project-description">Description</Label>
          <Textarea
            id="project-description"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        {isEdit && (
          <div className="space-y-2">
            <Label htmlFor="project-status">Statut</Label>
            <Select
              id="project-status"
              value={status}
              onChange={(e) => setStatus(e.target.value as ProjectStatus)}
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="ARCHIVED">ARCHIVED</option>
            </Select>
          </div>
        )}
        <div className="space-y-2">
          <Label htmlFor="project-client">Client</Label>
          <Select
            id="project-client"
            value={clientId}
            onChange={(e) => setClientId(e.target.value)}
            required
          >
            <option value="">Sélectionner un client</option>
            {clients.map((client) => (
              <option key={client.id} value={client.id}>
                {client.name}
              </option>
            ))}
          </Select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="project-team">Équipe</Label>
          <Select
            id="project-team"
            value={teamId}
            onChange={(e) => {
              setTeamId(e.target.value);
              setMemberIds([]);
            }}
          >
            <option value="">Aucune équipe</option>
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name} — {team.managerName}
              </option>
            ))}
          </Select>
        </div>
        {selectedTeam && (
          <div className="space-y-2">
            <Label>Employés assignés au projet</Label>
            <div className="max-h-40 space-y-2 overflow-y-auto rounded-xl border border-border p-3">
              {selectedTeam.members.map((member) => (
                <label key={member.id} className="flex items-center gap-2 text-sm text-body">
                  <input
                    type="checkbox"
                    checked={memberIds.includes(member.id)}
                    onChange={() => toggleMember(member.id)}
                  />
                  {member.firstName} {member.lastName}
                </label>
              ))}
            </div>
          </div>
        )}
        {error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" loading={loading}>
            {isEdit ? "Enregistrer" : "Créer"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
