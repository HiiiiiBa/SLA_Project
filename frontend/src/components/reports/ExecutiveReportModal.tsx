"use client";

import { useEffect, useState } from "react";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { ApiError, apiFetch } from "@/lib/api";
import type { ExecutiveReport, ExecutiveReportRequest, Project } from "@/types";

interface ExecutiveReportModalProps {
  open: boolean;
  onClose: () => void;
  onGenerated: (report: ExecutiveReport) => void;
}

function toLocalDateTimeStart(date: string) {
  return `${date}T00:00:00`;
}

function toLocalDateTimeEnd(date: string) {
  return `${date}T23:59:59`;
}

function defaultPeriod() {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 30);
  const toInput = (value: Date) => value.toISOString().slice(0, 10);
  return { start: toInput(start), end: toInput(end) };
}

export function ExecutiveReportModal({
  open,
  onClose,
  onGenerated,
}: ExecutiveReportModalProps) {
  const defaults = defaultPeriod();
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectId, setProjectId] = useState("");
  const [periodStart, setPeriodStart] = useState(defaults.start);
  const [periodEnd, setPeriodEnd] = useState(defaults.end);
  const [loading, setLoading] = useState(false);
  const [loadingProjects, setLoadingProjects] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!open) return;
    const period = defaultPeriod();
    setPeriodStart(period.start);
    setPeriodEnd(period.end);
    setProjectId("");
    setError("");
    setLoadingProjects(true);
    apiFetch<Project[]>("/api/projects")
      .then((data) => {
        setProjects(data);
        if (data.length === 1) {
          setProjectId(String(data[0].id));
        }
      })
      .catch(() => setProjects([]))
      .finally(() => setLoadingProjects(false));
  }, [open]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError("");

    if (!projectId) {
      setError("Sélectionnez un projet.");
      return;
    }
    if (!periodStart || !periodEnd) {
      setError("Indiquez la période du rapport.");
      return;
    }
    if (periodStart > periodEnd) {
      setError("La date de début doit précéder la date de fin.");
      return;
    }

    const payload: ExecutiveReportRequest = {
      projectId: Number(projectId),
      periodStart: toLocalDateTimeStart(periodStart),
      periodEnd: toLocalDateTimeEnd(periodEnd),
    };

    setLoading(true);
    try {
      const report = await apiFetch<ExecutiveReport>("/api/ai/executive-report", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      onGenerated(report);
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Génération impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Generate AI Report"
      description="Sélectionnez un projet et une période. Gemini produira un executive report structuré."
      size="large"
    >
      <form className="space-y-5" onSubmit={handleSubmit}>
        <div className="space-y-2">
          <Label htmlFor="exec-project">Projet</Label>
          <Select
            id="exec-project"
            value={projectId}
            onChange={(event) => setProjectId(event.target.value)}
            disabled={loadingProjects || loading}
            required
          >
            <option value="">
              {loadingProjects ? "Chargement..." : "Choisir un projet"}
            </option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>
                {project.name}
                {project.clientName ? ` (${project.clientName})` : ""}
                {project.slaName ? ` · ${project.slaName}` : " · sans SLA"}
              </option>
            ))}
          </Select>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="exec-start">Date de début</Label>
            <Input
              id="exec-start"
              type="date"
              value={periodStart}
              onChange={(event) => setPeriodStart(event.target.value)}
              disabled={loading}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="exec-end">Date de fin</Label>
            <Input
              id="exec-end"
              type="date"
              value={periodEnd}
              onChange={(event) => setPeriodEnd(event.target.value)}
              disabled={loading}
              required
            />
          </div>
        </div>

        {error && (
          <div className="rounded-xl border border-error/30 bg-error/10 px-4 py-3 text-sm text-error">
            {error}
          </div>
        )}

        <div className="flex justify-end gap-3 border-t border-border/60 pt-4">
          <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
            Annuler
          </Button>
          <Button type="submit" loading={loading}>
            <Sparkles className="h-4 w-4" />
            Générer avec Gemini
          </Button>
        </div>
      </form>
    </Modal>
  );
}
