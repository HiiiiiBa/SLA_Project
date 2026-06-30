"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { Eye, Pencil, Plus, Trash2 } from "lucide-react";
import { SlaFormModal } from "@/components/forms/SlaFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatPercent } from "@/lib/utils";
import type { Sla } from "@/types";

export default function SlasPage() {
  const { isAdmin } = useAuth();
  const [slas, setSlas] = useState<Sla[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedSla, setSelectedSla] = useState<Sla | null>(null);

  const loadSlas = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<Sla[]>("/api/slas")
      .then(setSlas)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadSlas();
  }, [loadSlas]);

  async function handleDelete(sla: Sla) {
    if (!confirm(`Supprimer le SLA "${sla.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/slas/${sla.id}`, { method: "DELETE" });
      loadSlas();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Contrats SLA"
        description="Liste des accords de niveau de service monitorés et leurs seuils."
        action={
          isAdmin ? (
            <Button
              onClick={() => {
                setSelectedSla(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouveau SLA
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadSlas} />}

      <Card>
        <CardHeader
          title="Tous les SLA"
          description={`${slas.length} contrat(s) enregistré(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  <th className="px-6 py-4 font-medium">Uptime cible</th>
                  <th className="px-6 py-4 font-medium">Temps réponse max</th>
                  <th className="px-6 py-4 font-medium">Taux erreur max</th>
                  <th className="px-6 py-4 font-medium">Client</th>
                  <th className="px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {slas.map((sla) => (
                  <tr key={sla.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{sla.name}</td>
                    <td className="px-6 py-4">
                      <StatusBadge status={sla.status} />
                    </td>
                    <td className="px-6 py-4 text-body">{formatPercent(sla.uptimeTarget)}</td>
                    <td className="px-6 py-4 text-body">{sla.responseTimeLimit} ms</td>
                    <td className="px-6 py-4 text-body">{formatPercent(sla.errorRateLimit)}</td>
                    <td className="px-6 py-4 text-body">#{sla.clientId}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-2">
                        <Link href={`/slas/${sla.id}`}>
                          <Button variant="secondary">
                            <Eye className="h-4 w-4" />
                          </Button>
                        </Link>
                        {isAdmin && (
                          <>
                            <Button
                              variant="secondary"
                              onClick={() => {
                                setSelectedSla(sla);
                                setModalOpen(true);
                              }}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            <Button variant="danger" onClick={() => handleDelete(sla)}>
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && slas.length === 0 && (
            <div className="px-6 py-10 text-sm text-muted">Aucun SLA trouvé.</div>
          )}
        </CardBody>
      </Card>

      {isAdmin && (
        <SlaFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadSlas}
          sla={selectedSla}
        />
      )}
    </>
  );
}
