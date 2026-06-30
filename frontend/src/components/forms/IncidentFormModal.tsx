"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { ApiError, apiFetch } from "@/lib/api";
import { nowForInput, toApiDateTime, toInputDateTime } from "@/lib/datetime";
import type {
  Incident,
  IncidentCreateRequest,
  IncidentSeverity,
  IncidentUpdateRequest,
  Sla,
} from "@/types";

interface IncidentFormModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  incident?: Incident | null;
  defaultSlaId?: number;
}

const severities: IncidentSeverity[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

export function IncidentFormModal({
  open,
  onClose,
  onSaved,
  incident,
  defaultSlaId,
}: IncidentFormModalProps) {
  const isEdit = Boolean(incident);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [slaId, setSlaId] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [severity, setSeverity] = useState<IncidentSeverity>("MEDIUM");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) return;
    apiFetch<Sla[]>("/api/slas").then(setSlas).catch(() => setSlas([]));
  }, [open]);

  useEffect(() => {
    if (!open) return;
    setSlaId(String(incident?.slaId ?? defaultSlaId ?? ""));
    setStartTime(toInputDateTime(incident?.startTime) || nowForInput());
    setEndTime(toInputDateTime(incident?.endTime));
    setSeverity(incident?.severity ?? "MEDIUM");
    setDescription(incident?.description ?? "");
    setError("");
  }, [open, incident, defaultSlaId]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      if (isEdit && incident) {
        const payload: IncidentUpdateRequest = {
          startTime: toApiDateTime(startTime),
          endTime: endTime ? toApiDateTime(endTime) : null,
          severity,
          description,
        };
        await apiFetch<Incident>(`/api/incidents/${incident.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        const payload: IncidentCreateRequest = {
          startTime: toApiDateTime(startTime),
          severity,
          description,
          slaId: Number(slaId),
        };
        await apiFetch<Incident>("/api/incidents", {
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
      title={isEdit ? "Modifier l'incident" : "Nouvel incident"}
      description="Documentez un incident impactant un SLA."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {!isEdit && (
          <div className="space-y-2">
            <Label htmlFor="incident-sla">SLA concerné</Label>
            <Select
              id="incident-sla"
              value={slaId}
              onChange={(e) => setSlaId(e.target.value)}
              required
            >
              <option value="">Sélectionner un SLA</option>
              {slas.map((sla) => (
                <option key={sla.id} value={sla.id}>
                  {sla.name}
                </option>
              ))}
            </Select>
          </div>
        )}
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="incident-start">Début</Label>
            <Input
              id="incident-start"
              type="datetime-local"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              required
            />
          </div>
          {isEdit && (
            <div className="space-y-2">
              <Label htmlFor="incident-end">Fin</Label>
              <Input
                id="incident-end"
                type="datetime-local"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
              />
            </div>
          )}
        </div>
        <div className="space-y-2">
          <Label htmlFor="incident-severity">Sévérité</Label>
          <Select
            id="incident-severity"
            value={severity}
            onChange={(e) => setSeverity(e.target.value as IncidentSeverity)}
          >
            {severities.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </Select>
        </div>
        <div className="space-y-2">
          <Label htmlFor="incident-description">Description</Label>
          <Textarea
            id="incident-description"
            rows={4}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
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
