"use client";

import { useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { ApiError, apiFetch } from "@/lib/api";
import type { User } from "@/types";

interface ResetPasswordModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  user: User | null;
}

export function ResetPasswordModal({ open, onClose, onSaved, user }: ResetPasswordModalProps) {
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleClose() {
    setPassword("");
    setConfirmPassword("");
    setError("");
    onClose();
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!user) return;

    if (password.length < 8) {
      setError("Le mot de passe doit contenir au moins 8 caractères");
      return;
    }
    if (password !== confirmPassword) {
      setError("Les mots de passe ne correspondent pas");
      return;
    }

    setLoading(true);
    setError("");
    try {
      await apiFetch<User>(`/api/admin/users/${user.id}/reset-password`, {
        method: "PATCH",
        body: JSON.stringify({ password }),
      });
      onSaved();
      handleClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Réinitialisation impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={handleClose}
      title="Réinitialiser le mot de passe"
      description={user ? `Nouveau mot de passe pour ${user.email}` : undefined}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="reset-password">Nouveau mot de passe</Label>
          <Input
            id="reset-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="reset-confirm">Confirmer le mot de passe</Label>
          <Input
            id="reset-confirm"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            minLength={8}
          />
        </div>
        {error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={handleClose}>
            Annuler
          </Button>
          <Button type="submit" loading={loading}>
            Réinitialiser
          </Button>
        </div>
      </form>
    </Modal>
  );
}
