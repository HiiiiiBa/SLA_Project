"use client";

import { Archive, PauseCircle, PlayCircle } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { ApiError, apiFetch } from "@/lib/api";
import type { Sla } from "@/types";

interface SlaLifecycleActionsProps {
  sla: Sla;
  onChanged: () => void;
  onError?: (message: string) => void;
  compact?: boolean;
}

export function SlaLifecycleActions({
  sla,
  onChanged,
  onError,
  compact = false,
}: SlaLifecycleActionsProps) {
  async function runAction(
    path: string,
    confirmMessage: string,
    successLabel: string,
  ) {
    if (!confirm(confirmMessage)) return;
    try {
      await apiFetch<Sla>(path, { method: "PATCH" });
      onChanged();
    } catch (err) {
      onError?.(err instanceof ApiError ? err.message : `${successLabel} impossible`);
    }
  }

  const canActivate =
    sla.status === "INACTIVE" || sla.status === "WARNING" || sla.status === "BREACHED";
  const canDeactivate = sla.status !== "INACTIVE" && sla.status !== "ARCHIVED";
  const canArchive = sla.status !== "ARCHIVED";

  const iconButtonClass = compact ? "!px-2.5 !py-2" : undefined;

  if (compact) {
    return (
      <>
        {canActivate && (
          <Button
            variant="secondary"
            className={iconButtonClass}
            title="Activer"
            onClick={() =>
              runAction(
                `/api/slas/${sla.id}/activate`,
                `Activer le SLA "${sla.name}" ?`,
                "Activation",
              )
            }
          >
            <PlayCircle className="h-4 w-4" />
          </Button>
        )}
        {canDeactivate && (
          <Button
            variant="secondary"
            className={iconButtonClass}
            title="Désactiver"
            onClick={() =>
              runAction(
                `/api/slas/${sla.id}/deactivate`,
                `Désactiver le SLA "${sla.name}" ? Il ne sera plus évalué par le moteur SLA.`,
                "Désactivation",
              )
            }
          >
            <PauseCircle className="h-4 w-4" />
          </Button>
        )}
        {canArchive && (
          <Button
            variant="secondary"
            className={iconButtonClass}
            title="Archiver"
            onClick={() =>
              runAction(
                `/api/slas/${sla.id}/archive`,
                `Archiver le SLA "${sla.name}" ? Cette action le retire du monitoring actif.`,
                "Archivage",
              )
            }
          >
            <Archive className="h-4 w-4" />
          </Button>
        )}
      </>
    );
  }

  return (
    <div className="mt-2 flex flex-wrap gap-2">
      {canActivate && (
        <Button
          variant="secondary"
          onClick={() =>
            runAction(
              `/api/slas/${sla.id}/activate`,
              `Activer le SLA "${sla.name}" ?`,
              "Activation",
            )
          }
        >
          <PlayCircle className="h-4 w-4" />
          Activer
        </Button>
      )}
      {canDeactivate && (
        <Button
          variant="secondary"
          onClick={() =>
            runAction(
              `/api/slas/${sla.id}/deactivate`,
              `Désactiver le SLA "${sla.name}" ? Il ne sera plus évalué par le moteur SLA.`,
              "Désactivation",
            )
          }
        >
          <PauseCircle className="h-4 w-4" />
          Désactiver
        </Button>
      )}
      {canArchive && (
        <Button
          variant="secondary"
          onClick={() =>
            runAction(
              `/api/slas/${sla.id}/archive`,
              `Archiver le SLA "${sla.name}" ? Cette action le retire du monitoring actif.`,
              "Archivage",
            )
          }
        >
          <Archive className="h-4 w-4" />
          Archiver
        </Button>
      )}
    </div>
  );
}
