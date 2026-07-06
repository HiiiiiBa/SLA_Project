import { ApiError, apiFetch } from "@/lib/api";
import type {
  ApprovalActionType,
  ApprovalRequest,
  ApprovalRequestCreatePayload,
  ApprovalTargetType,
} from "@/types";

export function approvalActionLabel(actionType: ApprovalActionType): string {
  switch (actionType) {
    case "DELETE_PROJECT":
      return "Suppression de projet";
    case "DELETE_TEAM":
      return "Suppression d'équipe";
    case "DELETE_SLA":
      return "Suppression de SLA";
    case "ARCHIVE_SLA":
      return "Archivage de SLA";
    case "ACTIVATE_SLA":
      return "Activation de SLA";
    case "DEACTIVATE_SLA":
      return "Désactivation de SLA";
  }
}

export async function submitApprovalRequest(
  payload: ApprovalRequestCreatePayload,
): Promise<ApprovalRequest> {
  return apiFetch<ApprovalRequest>("/api/approval-requests", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function requestApprovalAction(options: {
  actionType: ApprovalActionType;
  targetType: ApprovalTargetType;
  targetId: number;
  targetLabel: string;
  confirmMessage: string;
  reason?: string;
}): Promise<ApprovalRequest> {
  if (!confirm(options.confirmMessage)) {
    throw new ApiError("Demande annulée", 0);
  }
  return submitApprovalRequest({
    actionType: options.actionType,
    targetType: options.targetType,
    targetId: options.targetId,
    reason: options.reason,
  });
}
