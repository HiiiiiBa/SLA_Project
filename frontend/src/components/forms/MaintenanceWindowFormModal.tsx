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
  MaintenanceWindow,
  MaintenanceWindowCreateRequest,
  MaintenanceWindowUpdateRequest,
  ServiceEntity,
  Sla,
} from "@/types";

interface MaintenanceWindowFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  window?: MaintenanceWindow | null;
  defaultSlaId?: number;
}

function toLocalInputValue(iso?: string) {
  if (!iso) return "";
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return iso.slice(0, 16);
  }
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function toApiDateTime(localValue: string) {
  if (!localValue) return "";
  return localValue.length === 16 ? `${localValue}:00` : localValue;
}

export function MaintenanceWindowFormModal({
  open,
  onClose,
  onSaved,
  window: editing,
  defaultSlaId,
}: MaintenanceWindowFormModalProps) {
  const isEdit = Boolean(editing);
  const [title, setTitle] = useState("");
  const [reason, setReason] = useState("");
  const [slaId, setSlaId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [slas, setSlas] = useState<Sla[]>([]);
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const filteredServices = useMemo(() => {
    if (!slaId) return [];
    return services.filter((service) => String(service.slaId) === slaId);
  }, [services, slaId]);

  useEffect(() => {
    if (!open) return;
    Promise.all([
      apiFetch<Sla[]>("/api/slas"),
      apiFetch<ServiceEntity[]>("/api/services"),
    ])
      .then(([slaData, serviceData]) => {
        setSlas(slaData);
        setServices(serviceData);
      })
      .catch(() => {
        setSlas([]);
        setServices([]);
      });
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setTitle(editing?.title ?? "");
    setReason(editing?.reason ?? "");
    setSlaId(String(editing?.slaId ?? defaultSlaId ?? ""));
    setServiceId(editing?.serviceId ? String(editing.serviceId) : "");
    setStartTime(toLocalInputValue(editing?.startTime));
    setEndTime(toLocalInputValue(editing?.endTime));
    setError("");
  }, [open, editing, defaultSlaId]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      if (isEdit && editing) {
        const payload: MaintenanceWindowUpdateRequest = {
          title: title.trim(),
          reason: reason.trim() || undefined,
          serviceId: serviceId ? Number(serviceId) : undefined,
          startTime: toApiDateTime(startTime),
          endTime: toApiDateTime(endTime),
        };
        await apiFetch<MaintenanceWindow>(`/api/maintenance-windows/${editing.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: MaintenanceWindowCreateRequest = {
          title: title.trim(),
          reason: reason.trim() || undefined,
          slaId: Number(slaId),
          serviceId: serviceId ? Number(serviceId) : undefined,
          startTime: toApiDateTime(startTime),
          endTime: toApiDateTime(endTime),
        };
        await apiFetch<MaintenanceWindow>("/api/maintenance-windows", {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Enregistrement impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? "Modifier la maintenance" : "Nouvelle fenêtre de maintenance"}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <p className="rounded-lg border border-error/30 bg-error/10 px-3 py-2 text-sm text-error">
            {error}
          </p>
        )}

        <div>
          <Label htmlFor="mw-title">Titre</Label>
          <Input
            id="mw-title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            placeholder="Mise à jour nocturne"
          />
        </div>

        <div>
          <Label htmlFor="mw-sla">SLA</Label>
          <Select
            id="mw-sla"
            value={slaId}
            onChange={(e) => {
              setSlaId(e.target.value);
              setServiceId("");
            }}
            required
            disabled={isEdit}
          >
            <option value="">Sélectionner un SLA</option>
            {slas.map((sla) => (
              <option key={sla.id} value={sla.id}>
                {sla.name}
              </option>
            ))}
          </Select>
        </div>

        <div>
          <Label htmlFor="mw-service">Service (optionnel)</Label>
          <Select
            id="mw-service"
            value={serviceId}
            onChange={(e) => setServiceId(e.target.value)}
            disabled={!slaId}
          >
            <option value="">Tous les services du SLA</option>
            {filteredServices.map((service) => (
              <option key={service.id} value={service.id}>
                {service.name}
              </option>
            ))}
          </Select>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <Label htmlFor="mw-start">Début</Label>
            <Input
              id="mw-start"
              type="datetime-local"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              required
            />
          </div>
          <div>
            <Label htmlFor="mw-end">Fin</Label>
            <Input
              id="mw-end"
              type="datetime-local"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              required
            />
          </div>
        </div>

        <div>
          <Label htmlFor="mw-reason">Raison (optionnel)</Label>
          <Textarea
            id="mw-reason"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            placeholder="Déploiement, patch sécurité..."
          />
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? "Enregistrement..." : isEdit ? "Enregistrer" : "Créer"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
