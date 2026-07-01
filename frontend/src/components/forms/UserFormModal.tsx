"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { PasswordInput } from "@/components/ui/PasswordInput";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { ApiError, apiFetch } from "@/lib/api";
import type { Role, User, UserCreateRequest, UserUpdateRequest } from "@/types";

interface UserFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  user?: User | null;
}

const roles: Role[] = ["ADMIN", "USER", "CLIENT"];

export function UserFormModal({ open, onClose, onSaved, user }: UserFormModalProps) {
  const isEdit = Boolean(user);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("USER");
  const [enabled, setEnabled] = useState(true);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFirstName(user?.firstName ?? "");
    setLastName(user?.lastName ?? "");
    setEmail(user?.email ?? "");
    setPassword("");
    setRole(user?.role ?? "USER");
    setEnabled(user?.enabled ?? true);
    setError("");
  }, [open, user]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && user) {
        const payload: UserUpdateRequest = {
          firstName,
          lastName,
          email,
          role,
          enabled,
        };
        if (password) payload.password = password;
        await apiFetch<User>(`/api/admin/users/${user.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: UserCreateRequest = {
          firstName,
          lastName,
          email,
          password,
          role,
        };
        await apiFetch<User>("/api/admin/users", {
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
      title={isEdit ? "Modifier l'utilisateur" : "Nouvel utilisateur"}
      description="Gérez les comptes et leurs rôles."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="user-firstname">Prénom</Label>
            <Input id="user-firstname" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
          </div>
          <div className="space-y-2">
            <Label htmlFor="user-lastname">Nom</Label>
            <Input id="user-lastname" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="user-email">Email</Label>
          <Input id="user-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div className="space-y-2">
          <Label htmlFor="user-password">
            Mot de passe {isEdit && "(laisser vide pour ne pas changer)"}
          </Label>
          <PasswordInput
            id="user-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required={!isEdit}
            minLength={8}
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="user-role">Rôle</Label>
            <Select id="user-role" value={role} onChange={(e) => setRole(e.target.value as Role)}>
              {roles.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </Select>
          </div>
          {isEdit && (
            <div className="space-y-2">
              <Label htmlFor="user-enabled">Statut</Label>
              <Select
                id="user-enabled"
                value={enabled ? "true" : "false"}
                onChange={(e) => setEnabled(e.target.value === "true")}
              >
                <option value="true">Actif</option>
                <option value="false">Désactivé</option>
              </Select>
            </div>
          )}
        </div>
        {error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>Annuler</Button>
          <Button type="submit" loading={loading}>{isEdit ? "Enregistrer" : "Créer"}</Button>
        </div>
      </form>
    </Modal>
  );
}
