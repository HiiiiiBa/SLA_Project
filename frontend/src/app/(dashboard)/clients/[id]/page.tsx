"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Building2, Gauge, Server } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { ServiceStatusBadge, StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate, formatPercent } from "@/lib/utils";
import type { ClientPortfolio } from "@/types";

export default function ClientDetailPage() {
  const params = useParams();
  const clientId = Number(params.id);
  const [portfolio, setPortfolio] = useState<ClientPortfolio | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadPortfolio = useCallback(() => {
    if (!clientId) return;
    setLoading(true);
    setError(null);
    apiFetch<ClientPortfolio>(`/api/clients/${clientId}/portfolio`)
      .then(setPortfolio)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [clientId]);

  useEffect(() => {
    loadPortfolio();
  }, [loadPortfolio]);

  if (loading) {
    return <div className="py-20 text-center text-muted">Chargement du client...</div>;
  }

  if (!portfolio) {
    return (
      <EmptyState
        icon={Building2}
        title="Client introuvable"
        description="Ce client n'existe pas ou a été supprimé."
      />
    );
  }

  const { client, slas } = portfolio;
  const totalServices = slas.reduce((sum, sla) => sum + sla.services.length, 0);

  return (
    <>
      <div className="mb-6">
        <Link
          href="/clients"
          className="inline-flex items-center gap-2 text-sm text-muted transition hover:text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          Retour aux clients
        </Link>
      </div>

      <Header
        title={client.name}
        description={`${client.projectName ?? "Projet non renseigné"} — ${client.email}`}
      />

      {error && <ErrorBanner message={error} onRetry={loadPortfolio} />}

      <div className="mb-6 grid gap-4 md:grid-cols-3">
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">SLA</p>
            <p className="mt-2 text-2xl font-bold text-heading">{slas.length}</p>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Services</p>
            <p className="mt-2 text-2xl font-bold text-heading">{totalServices}</p>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <p className="text-xs font-semibold uppercase tracking-wider text-muted">Client depuis</p>
            <p className="mt-2 text-lg font-semibold text-heading">{formatDate(client.createdAt)}</p>
          </CardBody>
        </Card>
      </div>

      {slas.length === 0 ? (
        <EmptyState
          icon={Gauge}
          title="Aucun SLA pour ce client"
          description="Créez un contrat SLA et associez-le à ce client depuis la page SLA."
        />
      ) : (
        <div className="space-y-6">
          {slas.map((sla) => (
            <Card key={sla.id}>
              <CardHeader
                title={sla.name}
                description={`Uptime ${formatPercent(sla.uptimeTarget)} — ${sla.responseTimeLimit} ms — ${formatPercent(sla.errorRateLimit)} erreur`}
                action={
                  <div className="flex items-center gap-3">
                    <StatusBadge status={sla.status} />
                    <Link href={`/slas/${sla.id}`}>
                      <Button variant="secondary">Voir le détail</Button>
                    </Link>
                  </div>
                }
              />
              <CardBody className="overflow-x-auto p-0">
                {sla.services.length === 0 ? (
                  <div className="px-6 py-8 text-sm text-muted">
                    Aucun service associé à ce SLA.
                  </div>
                ) : (
                  <table className="min-w-full text-sm">
                    <thead className="table-head">
                      <tr>
                        <th className="px-6 py-4 font-medium">Service</th>
                        <th className="px-6 py-4 font-medium">Statut</th>
                        <th className="px-6 py-4 font-medium">Mis à jour</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sla.services.map((service) => (
                        <tr key={service.id} className="table-row">
                          <td className="px-6 py-4 font-medium text-heading">
                            <span className="inline-flex items-center gap-2">
                              <Server className="h-4 w-4 text-muted" />
                              {service.name}
                            </span>
                          </td>
                          <td className="px-6 py-4">
                            <ServiceStatusBadge status={service.status} />
                          </td>
                          <td className="px-6 py-4 text-muted">
                            {formatDate(service.updatedAt)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </CardBody>
            </Card>
          ))}
        </div>
      )}
    </>
  );
}
