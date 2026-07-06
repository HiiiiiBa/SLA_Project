"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { CheckCircle2, ClipboardCheck, XCircle } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { approvalActionLabel } from "@/lib/approval";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { ApprovalRequest } from "@/types";

export default function AdminApprovalsPage() {
  const { isAdmin } = useAuth();
  const router = useRouter();
  const [requests, setRequests] = useState<ApprovalRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [processingId, setProcessingId] = useState<number | null>(null);

  const loadRequests = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<ApprovalRequest[]>("/api/approval-requests?scope=pending")
      .then(setRequests)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!isAdmin) {
      router.replace("/dashboard");
      return;
    }
    loadRequests();
  }, [isAdmin, router, loadRequests]);

  async function handleApprove(request: ApprovalRequest) {
    const comment = window.prompt("Commentaire (optionnel)") ?? undefined;
    setProcessingId(request.id);
    setError(null);
    try {
      await apiFetch<ApprovalRequest>(`/api/approval-requests/${request.id}/approve`, {
        method: "PATCH",
        body: JSON.stringify({ comment: comment || undefined }),
      });
      loadRequests();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Validation impossible");
    } finally {
      setProcessingId(null);
    }
  }

  async function handleReject(request: ApprovalRequest) {
    const comment = window.prompt("Motif du refus (optionnel)") ?? undefined;
    setProcessingId(request.id);
    setError(null);
    try {
      await apiFetch<ApprovalRequest>(`/api/approval-requests/${request.id}/reject`, {
        method: "PATCH",
        body: JSON.stringify({ comment: comment || undefined }),
      });
      loadRequests();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Refus impossible");
    } finally {
      setProcessingId(null);
    }
  }

  return (
    <>
      <Header
        title="Validations admin"
        description="Approuvez ou refusez les demandes soumises par les managers."
      />

      {error && <ErrorBanner message={error} onRetry={loadRequests} />}

      <Card>
        <CardHeader
          title="Demandes en attente"
          description={`${requests.length} demande(s) à traiter`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : requests.length === 0 ? (
            <EmptyState
              icon={ClipboardCheck}
              title="Aucune demande en attente"
              description="Les managers vous notifieront ici lorsqu'une action sensible sera demandée."
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Date</th>
                  <th className="px-6 py-4 font-medium">Manager</th>
                  <th className="px-6 py-4 font-medium">Action</th>
                  <th className="px-6 py-4 font-medium">Cible</th>
                  <th className="px-6 py-4 font-medium">Motif</th>
                  <th className="px-6 py-4 font-medium">Décision</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request) => (
                  <tr key={request.id} className="table-row">
                    <td className="px-6 py-4 text-body">{formatDate(request.createdAt)}</td>
                    <td className="px-6 py-4 text-body">
                      <div className="font-medium text-heading">{request.requesterName}</div>
                      <div className="text-xs text-muted">{request.requesterEmail}</div>
                    </td>
                    <td className="px-6 py-4 text-body">
                      {approvalActionLabel(request.actionType)}
                    </td>
                    <td className="px-6 py-4 text-body">{request.targetLabel}</td>
                    <td className="max-w-xs px-6 py-4 text-body">{request.reason ?? "—"}</td>
                    <td className="whitespace-nowrap px-6 py-4">
                      <div className="inline-flex items-center gap-1.5">
                        <Button
                          variant="secondary"
                          className="!px-2.5 !py-2"
                          title="Approuver"
                          loading={processingId === request.id}
                          onClick={() => handleApprove(request)}
                        >
                          <CheckCircle2 className="h-4 w-4 text-success" />
                        </Button>
                        <Button
                          variant="danger"
                          className="!px-2.5 !py-2"
                          title="Refuser"
                          disabled={processingId === request.id}
                          onClick={() => handleReject(request)}
                        >
                          <XCircle className="h-4 w-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>
    </>
  );
}
