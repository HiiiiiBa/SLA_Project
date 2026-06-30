"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { ApiError, apiFetch } from "@/lib/api";
import type { Client, ClientCreateRequest, ClientUpdateRequest } from "@/types";

interface ClientFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  client?: Client | null;
}

export function ClientFormModal({
  open,
  onClose,
  onSaved,
  client,
}: ClientFormModalProps) {
  const isEdit = Boolean(client);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [projectName, setProjectName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    setName(client?.name ?? "");
    setEmail(client?.email ?? "");
    setProjectName(client?.projectName ?? "");
    setError("");
  }, [open, client]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && client) {
        const payload: ClientUpdateRequest = { name, email, projectName };
        await apiFetch<Client>(`/api/clients/${client.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: ClientCreateRequest = { name, email, projectName };
        await apiFetch<Client>("/api/clients", {
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
      title={isEdit ? "Modifier le client" : "Nouveau client"}
      description="Renseignez les informations du client."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="client-name">Nom</Label>
          <Input id="client-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div className="space-y-2">
          <Label htmlFor="client-email">Email</Label>
          <Input
            id="client-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="client-project">Projet</Label>
          <Input
            id="client-project"
            value={projectName}
            onChange={(e) => setProjectName(e.target.value)}
            placeholder="Optionnel"
          />
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
