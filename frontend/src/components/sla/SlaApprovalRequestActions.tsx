"use client";

import { RequestApprovalButton } from "@/components/approval/RequestApprovalButton";
import type { Sla } from "@/types";

interface SlaApprovalRequestActionsProps {
  sla: Sla;
  onError?: (message: string) => void;
  onSuccess?: () => void;
  compact?: boolean;
}

export function SlaApprovalRequestActions({
  sla,
  onError,
  onSuccess,
  compact = false,
}: SlaApprovalRequestActionsProps) {
  return (
    <RequestApprovalButton
      actionType="DELETE_SLA"
      targetType="SLA"
      targetId={sla.id}
      targetLabel={sla.name}
      confirmMessage={`Demander la suppression du SLA "${sla.name}" à l'admin ?`}
      title="Demander la suppression (validation admin)"
      className={compact ? "!px-2.5 !py-2" : undefined}
      onSuccess={onSuccess}
      onError={onError}
    />
  );
}
