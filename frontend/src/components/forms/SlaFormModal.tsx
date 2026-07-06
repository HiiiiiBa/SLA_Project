"use client";

import { useEffect, useState } from "react";
import { SlaServiceDraftList, type ServiceDraft } from "@/components/forms/SlaServiceDraftList";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { ApiError, apiFetch } from "@/lib/api";
import type {
  Client,
  Sla,
  SlaCreateRequest,
  SlaStatus,
  SlaUpdateRequest,
} from "@/types";

interface SlaFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  sla?: Sla | null;
}

const statuses: SlaStatus[] = ["ACTIVE", "INACTIVE", "WARNING", "BREACHED", "ARCHIVED"];

export function SlaFormModal({ open, onClose, onSaved, sla }: SlaFormModalProps) {
  const isEdit = Boolean(sla);
  const [clients, setClients] = useState<Client[]>([]);
  const [name, setName] = useState("");
  const [status, setStatus] = useState<SlaStatus>("ACTIVE");
  const [clientId, setClientId] = useState("");
  const [uptimeTarget, setUptimeTarget] = useState("99.9");
  const [responseTimeLimit, setResponseTimeLimit] = useState("500");
  const [errorRateLimit, setErrorRateLimit] = useState("1");
  const [serviceDrafts, setServiceDrafts] = useState<ServiceDraft[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    apiFetch<Client[]>("/api/clients")
      .then(setClients)
      .catch(() => setClients([]));
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setName(sla?.name ?? "");
    setStatus(sla?.status ?? "ACTIVE");
    setClientId(sla?.clientId ? String(sla.clientId) : "");
    setUptimeTarget(String(sla?.uptimeTarget ?? 99.9));
    setResponseTimeLimit(String(sla?.responseTimeLimit ?? 500));
    setErrorRateLimit(String(sla?.errorRateLimit ?? 1));
    setServiceDrafts([]);
    setError("");
  }, [open, sla]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && sla) {
        const payload: SlaUpdateRequest = {
          name,
          uptimeTarget: Number(uptimeTarget),
          responseTimeLimit: Number(responseTimeLimit),
          errorRateLimit: Number(errorRateLimit),
          clientId: Number(clientId),
        };
        await apiFetch<Sla>(`/api/slas/${sla.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const services = serviceDrafts
          .map((draft) => ({
            name: draft.name.trim(),
            status: draft.status,
          }))
          .filter((draft) => draft.name.length > 0);

        const payload: SlaCreateRequest = {
          name,
          status,
          uptimeTarget: Number(uptimeTarget),
          responseTimeLimit: Number(responseTimeLimit),
          errorRateLimit: Number(errorRateLimit),
          clientId: Number(clientId),
          services: services.length > 0 ? services : undefined,
        };
        await apiFetch<Sla>("/api/slas", {
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
      title={isEdit ? "Modifier le SLA" : "Nouveau SLA"}
      description={
        isEdit
          ? "Définissez les objectifs de niveau de service."
          : "Définissez le contrat et associez éventuellement des services monitorés."
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="sla-name">Nom</Label>
          <Input id="sla-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        {!isEdit && (
          <>
            <div className="space-y-2">
              <Label htmlFor="sla-client">Client</Label>
              <Select
                id="sla-client"
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
              <Label htmlFor="sla-status">Statut initial</Label>
              <Select
                id="sla-status"
                value={status}
                onChange={(e) => setStatus(e.target.value as SlaStatus)}
              >
                {statuses.map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </Select>
            </div>
            <SlaServiceDraftList drafts={serviceDrafts} onChange={setServiceDrafts} />
          </>
        )}
        {isEdit && (
          <div className="space-y-2">
            <Label htmlFor="sla-client-edit">Client associé</Label>
            <Select
              id="sla-client-edit"
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
        )}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="space-y-2">
            <Label htmlFor="sla-uptime">Uptime cible (%)</Label>
            <Input
              id="sla-uptime"
              type="number"
              min="90"
              max="100"
              step="0.1"
              value={uptimeTarget}
              onChange={(e) => setUptimeTarget(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="sla-response">Temps réponse (ms)</Label>
            <Input
              id="sla-response"
              type="number"
              min="1"
              value={responseTimeLimit}
              onChange={(e) => setResponseTimeLimit(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="sla-error">Taux erreur (%)</Label>
            <Input
              id="sla-error"
              type="number"
              min="0"
              step="0.1"
              value={errorRateLimit}
              onChange={(e) => setErrorRateLimit(e.target.value)}
              required
            />
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
