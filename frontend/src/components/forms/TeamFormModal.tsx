"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import type { Team, TeamCreateRequest, TeamUpdateRequest, User } from "@/types";

interface TeamFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  team?: Team | null;
}

export function TeamFormModal({ open, onClose, onSaved, team }: TeamFormModalProps) {
  const { user, isAdmin, isManager } = useAuth();
  const isEdit = Boolean(team);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [managerId, setManagerId] = useState("");
  const [memberIds, setMemberIds] = useState<number[]>([]);
  const [managers, setManagers] = useState<User[]>([]);
  const [employees, setEmployees] = useState<User[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open || (!isAdmin && !isManager)) return;
    apiFetch<User[]>("/api/org/users?role=MANAGER")
      .then(setManagers)
      .catch(() => setManagers([]));
    apiFetch<User[]>("/api/org/users?role=EMPLOYEE")
      .then(setEmployees)
      .catch(() => setEmployees([]));
  }, [open, isAdmin, isManager]);

  useEffect(() => {
    if (!open) return;
    setName(team?.name ?? "");
    setDescription(team?.description ?? "");
    setManagerId(
      String(team?.managerId ?? (isManager && user ? user.userId : "")),
    );
    setMemberIds(team?.members.map((m) => m.id) ?? []);
    setError("");
  }, [open, team, isManager, user]);

  function toggleMember(id: number) {
    setMemberIds((current) =>
      current.includes(id) ? current.filter((value) => value !== id) : [...current, id],
    );
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    const payload = {
      name,
      description,
      managerId: Number(managerId),
      memberIds,
    };

    try {
      if (isEdit && team) {
        await apiFetch<Team>(`/api/teams/${team.id}`, {
          method: "PUT",
          body: JSON.stringify(payload satisfies TeamUpdateRequest),
        });
      } else {
        await apiFetch<Team>("/api/teams", {
          method: "POST",
          body: JSON.stringify(payload satisfies TeamCreateRequest),
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
      title={isEdit ? "Modifier l'équipe" : "Nouvelle équipe"}
      description="Manager + employés (ex. Team Dev, Team Réseaux)."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="team-name">Nom</Label>
          <Input id="team-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div className="space-y-2">
          <Label htmlFor="team-description">Description</Label>
          <Textarea
            id="team-description"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="team-manager">Manager</Label>
          <Select
            id="team-manager"
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            required
            disabled={isManager && !isAdmin}
          >
            <option value="">Sélectionner un manager</option>
            {managers.map((manager) => (
              <option key={manager.id} value={manager.id}>
                {manager.firstName} {manager.lastName} ({manager.email})
              </option>
            ))}
          </Select>
        </div>
        <div className="space-y-2">
          <Label>Employés de l&apos;équipe</Label>
          <div className="max-h-40 space-y-2 overflow-y-auto rounded-xl border border-border p-3">
            {employees.length === 0 ? (
              <p className="text-sm text-muted">Aucun employé disponible</p>
            ) : (
              employees.map((employee) => (
                <label key={employee.id} className="flex items-center gap-2 text-sm text-body">
                  <input
                    type="checkbox"
                    checked={memberIds.includes(employee.id)}
                    onChange={() => toggleMember(employee.id)}
                  />
                  {employee.firstName} {employee.lastName} ({employee.email})
                </label>
              ))
            )}
          </div>
        </div>
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
