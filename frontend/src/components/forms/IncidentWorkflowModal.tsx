"use client";

import { useCallback, useEffect, useState } from "react";
import { CheckCircle2, UserCheck } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Label } from "@/components/ui/Label";
import { Modal } from "@/components/ui/Modal";
import { Select } from "@/components/ui/Select";
import { Textarea } from "@/components/ui/Textarea";
import { IncidentStatusBadge, SeverityBadge } from "@/components/ui/Badge";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type {
  Incident,
  IncidentComment,
  IncidentCommentCreateRequest,
  IncidentStatus,
  IncidentStatusChangeRequest,
  IncidentUpdateRequest,
  Project,
  Sla,
  User,
} from "@/types";

interface IncidentWorkflowModalProps {
  open: boolean;
  onClose: () => void;
  onSaved: () => void;
  incident: Incident;
}

export function IncidentWorkflowModal({
  open,
  onClose,
  onSaved,
  incident,
}: IncidentWorkflowModalProps) {
  const { isEmployee, isManager, isAdmin, canModifyIncident, canAssignIncident } = useAuth();
  const sessionUserId = useSessionUserId();
  const [comments, setComments] = useState<IncidentComment[]>([]);
  const [candidates, setCandidates] = useState<User[]>([]);
  const [description, setDescription] = useState("");
  const [assigneeId, setAssigneeId] = useState("");
  const [commentText, setCommentText] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [commentLoading, setCommentLoading] = useState(false);

  const isResolved = incident.status === "RESOLVED";
  const isAssignedToSelf =
    incident.assigneeId != null
    && sessionUserId != null
    && incident.assigneeId === sessionUserId;
  const canEditEmployee =
    isEmployee && canModifyIncident && isAssignedToSelf && !isResolved;
  const canEditManager = (isAdmin || isManager) && canModifyIncident && !isResolved;
  const canComment = canEditEmployee || canEditManager;
  const canResolve =
    isEmployee && canEditEmployee && incident.status === "IN_PROGRESS";
  const canAssign =
    canAssignIncident && !isResolved && incident.status !== "RESOLVED";

  const loadComments = useCallback(() => {
    if (!open || !incident.id) return;
    apiFetch<IncidentComment[]>(`/api/incidents/${incident.id}/comments`)
      .then(setComments)
      .catch(() => setComments([]));
  }, [open, incident.id]);

  const loadCandidates = useCallback(() => {
    if (!open || !canAssignIncident) return;

    if (isAdmin) {
      const resolveClientId = async (): Promise<number | undefined> => {
        if (incident.projectId) {
          const projects = await apiFetch<Project[]>("/api/projects");
          return projects.find((item) => item.id === incident.projectId)?.clientId;
        }
        const sla = await apiFetch<Sla>(`/api/slas/${incident.slaId}`);
        return sla.clientId;
      };

      resolveClientId()
        .then((clientId) => {
          const url = clientId
            ? `/api/org/users?role=MANAGER&clientId=${clientId}`
            : "/api/org/users?role=MANAGER";
          return apiFetch<User[]>(url);
        })
        .then(setCandidates)
        .catch(() => setCandidates([]));
      return;
    }

    if (!isManager) {
      setCandidates([]);
      return;
    }

    if (incident.projectId) {
      apiFetch<Project[]>("/api/projects")
        .then((projects) => {
          const project = projects.find((item) => item.id === incident.projectId);
          if (!project?.assignedMembers?.length) {
            setCandidates([]);
            return;
          }
          setCandidates(
            project.assignedMembers.map((member) => ({
              id: member.id,
              firstName: member.firstName,
              lastName: member.lastName,
              email: member.email,
              role: "EMPLOYEE",
              enabled: true,
              createdAt: "",
              updatedAt: "",
            })),
          );
        })
        .catch(() => setCandidates([]));
      return;
    }

    apiFetch<User[]>("/api/org/users?role=EMPLOYEE")
      .then(setCandidates)
      .catch(() => setCandidates([]));
  }, [open, canAssignIncident, isAdmin, isManager, incident.projectId, incident.slaId]);

  useEffect(() => {
    if (!open) return;
    loadComments();
    loadCandidates();
    setDescription(incident.description);
    setAssigneeId(incident.assigneeId ? String(incident.assigneeId) : "");
    setCommentText("");
    setError("");
  }, [open, incident, loadComments, loadCandidates]);

  async function handleSaveDescription(event: React.FormEvent) {
    event.preventDefault();
    if (!canEditEmployee && !canEditManager) return;
    setLoading(true);
    setError("");
    try {
      const payload: IncidentUpdateRequest = {
        startTime: incident.startTime,
        endTime: incident.endTime ?? null,
        severity: incident.severity,
        description,
        projectId: incident.projectId,
      };
      await apiFetch<Incident>(`/api/incidents/${incident.id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Erreur lors de la mise à jour");
    } finally {
      setLoading(false);
    }
  }

  async function handleStatusChange(status: IncidentStatus) {
    if (!canResolve) return;
    setLoading(true);
    setError("");
    try {
      const payload: IncidentStatusChangeRequest = { status };
      await apiFetch<Incident>(`/api/incidents/${incident.id}/status`, {
        method: "PATCH",
        body: JSON.stringify(payload),
      });
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Changement de statut impossible");
    } finally {
      setLoading(false);
    }
  }

  async function handleAssign() {
    if (!canAssign) return;
    setLoading(true);
    setError("");
    try {
      await apiFetch<Incident>(`/api/incidents/${incident.id}/assign`, {
        method: "PATCH",
        body: JSON.stringify({
          assigneeId: assigneeId ? Number(assigneeId) : null,
        }),
      });
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Assignation impossible");
    } finally {
      setLoading(false);
    }
  }

  async function handleAddComment(event: React.FormEvent) {
    event.preventDefault();
    if (!canComment || !commentText.trim()) return;
    setCommentLoading(true);
    setError("");
    try {
      const payload: IncidentCommentCreateRequest = { content: commentText.trim() };
      await apiFetch<IncidentComment>(`/api/incidents/${incident.id}/comments`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setCommentText("");
      loadComments();
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Commentaire impossible");
    } finally {
      setCommentLoading(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={`Incident #${incident.id}`}
      description={
        isEmployee
          ? "Mettez à jour et résolvez l'incident qui vous est assigné."
          : "Assignation, suivi, commentaires et résolution."
      }
    >
      <div className="space-y-6">
        <div className="flex flex-wrap items-center gap-3">
          <IncidentStatusBadge status={incident.status} />
          <SeverityBadge severity={incident.severity} />
          <span className="text-sm text-muted">
            SLA #{incident.slaId}
            {incident.projectName ? ` · ${incident.projectName}` : ""}
          </span>
          {incident.assigneeName ? (
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
              Assigné à {incident.assigneeName}
            </span>
          ) : (
            <span className="rounded-full bg-card px-3 py-1 text-xs text-muted ring-1 ring-border">
              Non assigné
            </span>
          )}
        </div>

        {canAssign && (
          <div className="space-y-3 rounded-xl border border-border bg-card/50 p-4">
            <Label htmlFor="workflow-assignee">
              {isAdmin ? "Assigner à un manager" : "Assigner à un employé"}
            </Label>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
              <Select
                id="workflow-assignee"
                className="flex-1"
                value={assigneeId}
                onChange={(e) => setAssigneeId(e.target.value)}
              >
                <option value="">Non assigné</option>
                {candidates.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.firstName} {user.lastName} ({user.email})
                  </option>
                ))}
              </Select>
              <Button type="button" loading={loading} onClick={handleAssign}>
                <UserCheck className="h-4 w-4" />
                {isAdmin ? "Assigner au manager" : "Assigner à l'employé"}
              </Button>
            </div>
            <p className="text-xs text-muted">
              L&apos;assignation passe l&apos;incident en statut « En cours » si nécessaire.
            </p>
          </div>
        )}

        {(canEditEmployee || canEditManager) ? (
          <form onSubmit={handleSaveDescription} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="workflow-description">Description</Label>
              <Textarea
                id="workflow-description"
                rows={4}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-wrap gap-2">
              <Button type="submit" loading={loading}>
                Enregistrer la description
              </Button>
              {canResolve && (
                <Button
                  type="button"
                  variant="secondary"
                  loading={loading}
                  onClick={() => handleStatusChange("RESOLVED")}
                >
                  <CheckCircle2 className="h-4 w-4" />
                  Résoudre
                </Button>
              )}
            </div>
          </form>
        ) : (
          <div className="space-y-3 rounded-xl border border-border bg-card/50 p-4 text-sm text-body">
            <p>{incident.description}</p>
            <p className="text-muted">
              Début : {formatDate(incident.startTime)}
              {incident.endTime ? ` · Fin : ${formatDate(incident.endTime)}` : ""}
            </p>
          </div>
        )}

        <div className="space-y-3">
          <h3 className="text-sm font-semibold uppercase tracking-wider text-muted">Commentaires</h3>
          <div className="max-h-48 space-y-3 overflow-y-auto">
            {comments.map((comment) => (
              <div key={comment.id} className="rounded-xl border border-border/60 bg-card/50 p-3">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm font-medium text-heading">{comment.authorName}</span>
                  <span className="text-xs text-muted">{formatDate(comment.createdAt)}</span>
                </div>
                <p className="mt-2 text-sm text-body">{comment.content}</p>
              </div>
            ))}
            {comments.length === 0 && (
              <p className="text-sm text-muted">Aucun commentaire pour le moment.</p>
            )}
          </div>
          {canComment && (
            <form onSubmit={handleAddComment} className="space-y-2">
              <Textarea
                rows={3}
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder="Ajouter un commentaire de suivi..."
                required
              />
              <Button type="submit" variant="secondary" loading={commentLoading}>
                Publier le commentaire
              </Button>
            </form>
          )}
        </div>

        {error && (
          <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}
      </div>
    </Modal>
  );
}
