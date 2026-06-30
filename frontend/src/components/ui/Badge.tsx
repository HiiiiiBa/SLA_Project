import { cn } from "@/lib/utils";
import type { AlertStatus, SlaStatus } from "@/types";

const slaStyles: Record<SlaStatus, string> = {
  ACTIVE: "bg-emerald-50 text-emerald-700 ring-emerald-600/20",
  WARNING: "bg-amber-50 text-amber-700 ring-amber-600/20",
  BREACHED: "bg-red-50 text-red-700 ring-red-600/20",
  ARCHIVED: "bg-slate-100 text-slate-600 ring-slate-500/20",
};

const alertStyles: Record<AlertStatus, string> = {
  NEW: "bg-blue-50 text-blue-700 ring-blue-600/20",
  READ: "bg-amber-50 text-amber-700 ring-amber-600/20",
  RESOLVED: "bg-emerald-50 text-emerald-700 ring-emerald-600/20",
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
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium ring-1 ring-inset",
        styles,
      )}
    >
      {status}
    </span>
  );
}
