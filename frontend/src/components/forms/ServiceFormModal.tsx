"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { ApiError, apiFetch } from "@/lib/api";
import type {
  ServiceCreateRequest,
  ServiceEntity,
  ServiceStatus,
  ServiceUpdateRequest,
  Sla,
} from "@/types";

interface ServiceFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  service?: ServiceEntity | null;
  defaultSlaId?: number;
}

const statuses: ServiceStatus[] = ["UP", "DOWN"];

export function ServiceFormModal({ open, onClose, onSaved, service, defaultSlaId }: ServiceFormModalProps) {
  const isEdit = Boolean(service);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [name, setName] = useState("");
  const [status, setStatus] = useState<ServiceStatus>("UP");
  const [slaId, setSlaId] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    apiFetch<Sla[]>("/api/slas").then(setSlas).catch(() => setSlas([]));
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setName(service?.name ?? "");
    setStatus(service?.status ?? "UP");
    setSlaId(service?.slaId ? String(service.slaId) : defaultSlaId ? String(defaultSlaId) : "");
    setError("");
  }, [open, service]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && service) {
        const payload: ServiceUpdateRequest = {
          name,
          status,
          slaId: Number(slaId),
        };
        await apiFetch<ServiceEntity>(`/api/services/${service.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: ServiceCreateRequest = {
          name,
          status,
          slaId: Number(slaId),
        };
        await apiFetch<ServiceEntity>("/api/services", {
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
      title={isEdit ? "Modifier le service" : "Nouveau service"}
      description="Services monitorés rattachés à un SLA."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="service-name">Nom</Label>
          <Input id="service-name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        {!isEdit ? (
          <div className="space-y-2">
            <Label htmlFor="service-sla">SLA</Label>
            <Select id="service-sla" value={slaId} onChange={(e) => setSlaId(e.target.value)} required>
              <option value="">Sélectionner un SLA</option>
              {slas.map((sla) => (
                <option key={sla.id} value={sla.id}>{sla.name}</option>
              ))}
            </Select>
          </div>
        ) : (
          <div className="space-y-2">
            <Label htmlFor="service-sla-edit">SLA associé</Label>
            <Select id="service-sla-edit" value={slaId} onChange={(e) => setSlaId(e.target.value)} required>
              <option value="">Sélectionner un SLA</option>
              {slas.map((sla) => (
                <option key={sla.id} value={sla.id}>{sla.name}</option>
              ))}
            </Select>
          </div>
        )}
        <div className="space-y-2">
          <Label htmlFor="service-status">Statut</Label>
          <Select id="service-status" value={status} onChange={(e) => setStatus(e.target.value as ServiceStatus)}>
            {statuses.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </Select>
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
