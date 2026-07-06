"use client";

import { useState, type ReactNode } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { ApiError } from "@/lib/api";
import { requestApprovalAction } from "@/lib/approval";
import type { ApprovalActionType, ApprovalTargetType } from "@/types";

interface RequestApprovalButtonProps {
  actionType: ApprovalActionType;
  targetType: ApprovalTargetType;
  targetId: number;
  targetLabel: string;
  confirmMessage: string;
  title?: string;
  variant?: "primary" | "secondary" | "ghost" | "danger";
  icon?: ReactNode;
  className?: string;
  onSuccess?: () => void;
  onError?: (message: string) => void;
}

export function RequestApprovalButton({
  actionType,
  targetType,
  targetId,
  targetLabel,
  confirmMessage,
  title = "Demander la suppression (validation admin)",
  variant = "danger",
  icon,
  className,
  onSuccess,
  onError,
}: RequestApprovalButtonProps) {
  const [loading, setLoading] = useState(false);

  async function handleClick() {
    setLoading(true);
    try {
      await requestApprovalAction({
        actionType,
        targetType,
        targetId,
        targetLabel,
        confirmMessage,
      });
      onSuccess?.();
    } catch (err) {
      if (err instanceof ApiError && err.message === "Demande annulée") {
        return;
      }
      onError?.(err instanceof ApiError ? err.message : "Envoi de la demande impossible");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Button
      variant={variant}
      className={className}
      title={title}
      loading={loading}
      onClick={handleClick}
    >
      {icon ?? <Trash2 className="h-4 w-4" />}
    </Button>
  );
}
