"use client";

import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Select } from "@/components/ui/Select";
import type { ServiceStatus } from "@/types";

export interface ServiceDraft {
  key: string;
  name: string;
  status: ServiceStatus;
}

interface SlaServiceDraftListProps {
  drafts: ServiceDraft[];
  onChange: (drafts: ServiceDraft[]) => void;
}

const statuses: ServiceStatus[] = ["UP", "DOWN"];

export function SlaServiceDraftList({ drafts, onChange }: SlaServiceDraftListProps) {
  function addDraft() {
    onChange([
      ...drafts,
      { key: crypto.randomUUID(), name: "", status: "UP" },
    ]);
  }

  function updateDraft(key: string, patch: Partial<ServiceDraft>) {
    onChange(drafts.map((draft) => (draft.key === key ? { ...draft, ...patch } : draft)));
  }

  function removeDraft(key: string) {
    onChange(drafts.filter((draft) => draft.key !== key));
  }

  return (
    <div className="space-y-3 rounded-xl border border-border/70 bg-card/40 p-4">
      <div className="flex items-center justify-between gap-3">
        <div>
          <Label>Services associés</Label>
          <p className="mt-1 text-xs text-muted">Optionnel — vous pourrez en ajouter plus tard.</p>
        </div>
        <Button type="button" variant="secondary" onClick={addDraft}>
          <Plus className="h-4 w-4" />
          Ajouter
        </Button>
      </div>

      {drafts.length === 0 ? (
        <p className="text-sm text-muted">Aucun service pour ce SLA.</p>
      ) : (
        <div className="space-y-3">
          {drafts.map((draft, index) => (
            <div
              key={draft.key}
              className="grid gap-3 rounded-lg border border-border/60 bg-card/60 p-3 sm:grid-cols-[1fr_auto_auto]"
            >
              <div className="space-y-2">
                <Label htmlFor={`service-draft-name-${draft.key}`}>Service {index + 1}</Label>
                <Input
                  id={`service-draft-name-${draft.key}`}
                  value={draft.name}
                  onChange={(e) => updateDraft(draft.key, { name: e.target.value })}
                  placeholder="Ex. API Gateway"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor={`service-draft-status-${draft.key}`}>Statut</Label>
                <Select
                  id={`service-draft-status-${draft.key}`}
                  value={draft.status}
                  onChange={(e) =>
                    updateDraft(draft.key, { status: e.target.value as ServiceStatus })
                  }
                >
                  {statuses.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </Select>
              </div>
              <div className="flex items-end">
                <Button
                  type="button"
                  variant="ghost"
                  className="text-error hover:bg-error/10"
                  onClick={() => removeDraft(draft.key)}
                  title="Retirer ce service"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
