import { cn } from "@/lib/utils";
import type { AlertStatus, IncidentSeverity, IncidentStatus, ServiceStatus, SlaStatus } from "@/types";

const badgeBase =
  "inline-flex items-center rounded-lg px-3 py-1.5 text-xs font-semibold uppercase tracking-wide";

const slaStyles: Record<SlaStatus, string> = {
  ACTIVE: "bg-success/15 text-success border border-success/30 shadow-sm",
  INACTIVE: "bg-muted/20 text-muted border border-muted/40 shadow-sm",
  WARNING: "bg-warning/15 text-warning border border-warning/30 shadow-sm",
  BREACHED: "bg-error/15 text-error border border-error/30 shadow-sm",
  ARCHIVED: "bg-muted/15 text-muted border border-muted/30 shadow-sm",
};

const slaLabels: Record<SlaStatus, string> = {
  ACTIVE: "Actif",
  INACTIVE: "Inactif",
  WARNING: "Alerte",
  BREACHED: "Violé",
  ARCHIVED: "Archivé",
};

const alertStyles: Record<AlertStatus, string> = {
  NEW: "bg-primary/15 text-primary border border-primary/30 shadow-sm",
  READ: "bg-warning/15 text-warning border border-warning/30 shadow-sm",
  RESOLVED: "bg-success/15 text-success border border-success/30 shadow-sm",
};

const serviceStyles: Record<ServiceStatus, string> = {
  UP: "bg-success/15 text-success border border-success/30 shadow-sm",
  DOWN: "bg-error/15 text-error border border-error/30 shadow-sm",
};

const severityStyles: Record<IncidentSeverity, string> = {
  LOW: "bg-primary/15 text-primary border border-primary/30 shadow-sm",
  MEDIUM: "bg-warning/15 text-warning border border-warning/30 shadow-sm",
  HIGH: "bg-error/15 text-error border border-error/30 shadow-sm",
  CRITICAL: "bg-error/20 text-error border border-error/40 shadow-sm ring-1 ring-error/20",
};

export function StatusBadge({
  status,
  kind = "sla",
}: {
  status: SlaStatus | AlertStatus;
  kind?: "sla" | "alert";
}) {
  const styles =
    kind === "alert"
      ? alertStyles[status as AlertStatus]
      : slaStyles[status as SlaStatus];

  return (
    <span className={cn(badgeBase, styles)}>
      {kind === "sla" ? slaLabels[status as SlaStatus] ?? status : status}
    </span>
  );
}

export function ServiceStatusBadge({ status }: { status: ServiceStatus }) {
  return (
    <span className={cn(badgeBase, serviceStyles[status])}>
      {status}
    </span>
  );
}

const incidentStatusStyles: Record<IncidentStatus, string> = {
  OPEN: "bg-primary/15 text-primary border border-primary/30 shadow-sm",
  IN_PROGRESS: "bg-warning/15 text-warning border border-warning/30 shadow-sm",
  RESOLVED: "bg-success/15 text-success border border-success/30 shadow-sm",
};

const incidentStatusLabels: Record<IncidentStatus, string> = {
  OPEN: "Ouvert",
  IN_PROGRESS: "En cours",
  RESOLVED: "Résolu",
};

export function IncidentStatusBadge({ status }: { status: IncidentStatus }) {
  return (
    <span className={cn(badgeBase, incidentStatusStyles[status])}>
      {incidentStatusLabels[status]}
    </span>
  );
}

export function SeverityBadge({ severity }: { severity: IncidentSeverity }) {
  return (
    <span className={cn(badgeBase, severityStyles[severity])}>
      {severity}
    </span>
  );
}
