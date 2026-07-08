"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import {
  AlertTriangle,
  ArrowRight,
  FolderKanban,
  Gauge,
  Search,
  Siren,
} from "lucide-react";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { buildProjectSummaries } from "@/lib/project-dashboard";
import { cn } from "@/lib/utils";
import type { Alert, Incident, Project, Report, Sla } from "@/types";

interface ProjectOverviewPanelProps {
  projects: Project[];
  slas: Sla[];
  alerts: Alert[];
  incidents: Incident[];
  reports: Report[];
}

export function ProjectOverviewPanel({
  projects,
  slas,
  alerts,
  incidents,
  reports,
}: ProjectOverviewPanelProps) {
  const [query, setQuery] = useState("");

  const summaries = useMemo(
    () => buildProjectSummaries(projects, slas, alerts, incidents, reports),
    [projects, slas, alerts, incidents, reports],
  );

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return summaries;
    return summaries.filter(
      ({ project, sla }) =>
        project.name.toLowerCase().includes(normalized)
        || project.clientName.toLowerCase().includes(normalized)
        || project.teamName?.toLowerCase().includes(normalized)
        || sla?.name.toLowerCase().includes(normalized),
    );
  }, [summaries, query]);

  if (projects.length === 0) {
    return (
      <Card>
        <CardBody className="py-12 text-center text-sm text-muted">
          Aucun projet à afficher pour votre périmètre.
        </CardBody>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader
        title="Projets"
        description="Accès direct au détail : SLA, services, incidents et alertes"
        action={
          <Link
            href="/projects"
            className="text-sm font-semibold text-primary hover:underline"
          >
            Tous les projets
          </Link>
        }
      />
      <CardBody className="space-y-4">
        {projects.length > 3 && (
          <div className="relative max-w-md">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
            <Input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Rechercher un projet ou client..."
              className="pl-9"
            />
          </div>
        )}

        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((summary) => {
            const { project, sla, openAlerts, openIncidents } = summary;
            const hasIssue = openAlerts.length > 0 || openIncidents.length > 0
              || sla?.status === "BREACHED" || sla?.status === "WARNING";

            return (
              <Link
                key={project.id}
                href={`/projects/${project.id}`}
                className={cn(
                  "group flex flex-col rounded-2xl border bg-card/40 p-4 transition hover:border-primary/40 hover:bg-card/70",
                  hasIssue ? "border-warning/30" : "border-border/70",
                )}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex min-w-0 items-start gap-3">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                      <FolderKanban className="h-5 w-5" />
                    </div>
                    <div className="min-w-0">
                      <p className="truncate font-semibold text-heading group-hover:text-primary">
                        {project.name}
                      </p>
                      <p className="mt-0.5 truncate text-xs text-muted">{project.clientName}</p>
                    </div>
                  </div>
                  <ArrowRight className="h-4 w-4 shrink-0 text-muted transition group-hover:text-primary" />
                </div>

                <div className="mt-4 flex flex-wrap gap-2">
                  <MetricPill
                    icon={<Gauge className="h-3.5 w-3.5" />}
                    label={sla?.status ?? "Sans SLA"}
                    tone={
                      sla?.status === "BREACHED"
                        ? "danger"
                        : sla?.status === "WARNING"
                          ? "warning"
                          : "neutral"
                    }
                  />
                  <MetricPill
                    icon={<AlertTriangle className="h-3.5 w-3.5" />}
                    label={`${openAlerts.length} alerte(s)`}
                    tone={openAlerts.length > 0 ? "warning" : "neutral"}
                  />
                  <MetricPill
                    icon={<Siren className="h-3.5 w-3.5" />}
                    label={`${openIncidents.length} incident(s)`}
                    tone={openIncidents.length > 0 ? "danger" : "neutral"}
                  />
                </div>
              </Link>
            );
          })}
        </div>

        {filtered.length === 0 && (
          <p className="py-8 text-center text-sm text-muted">
            Aucun projet ne correspond à votre recherche.
          </p>
        )}
      </CardBody>
    </Card>
  );
}

function MetricPill({
  icon,
  label,
  tone,
}: {
  icon: React.ReactNode;
  label: string;
  tone: "neutral" | "warning" | "danger";
}) {
  const tones = {
    neutral: "bg-card text-muted ring-border",
    warning: "bg-warning/10 text-warning ring-warning/30",
    danger: "bg-error/10 text-error ring-error/30",
  };

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-medium ring-1",
        tones[tone],
      )}
    >
      {icon}
      {label}
    </span>
  );
}
