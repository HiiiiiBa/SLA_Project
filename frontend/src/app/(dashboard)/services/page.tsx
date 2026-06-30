"use client";

import { useCallback, useEffect, useState } from "react";
import { Pencil, Plus, Server, Trash2 } from "lucide-react";
import { ServiceFormModal } from "@/components/forms/ServiceFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { ServiceStatusBadge } from "@/components/ui/Badge";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { ServiceEntity } from "@/types";

export default function ServicesPage() {
  const { isAdmin } = useAuth();
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedService, setSelectedService] = useState<ServiceEntity | null>(null);

  const loadServices = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<ServiceEntity[]>("/api/services")
      .then(setServices)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadServices();
  }, [loadServices]);

  async function handleDelete(service: ServiceEntity) {
    if (!confirm(`Supprimer le service "${service.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/services/${service.id}`, { method: "DELETE" });
      loadServices();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Services monitorés"
        description="Composants techniques rattachés aux contrats SLA."
        action={
          isAdmin ? (
            <Button
              onClick={() => {
                setSelectedService(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouveau service
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadServices} />}

      <Card>
        <CardHeader
          title="Liste des services"
          description={`${services.length} service(s) enregistré(s)`}
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
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Mis à jour</th>
                  {isAdmin && <th className="px-6 py-4 font-medium">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {services.map((service) => (
                  <tr key={service.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{service.name}</td>
                    <td className="px-6 py-4">
                      <ServiceStatusBadge status={service.status} />
                    </td>
                    <td className="px-6 py-4 text-body">
                      {service.slaName ?? `SLA #${service.slaId}`}
                    </td>
                    <td className="px-6 py-4 text-muted">{formatDate(service.updatedAt)}</td>
                    {isAdmin && (
                      <td className="px-6 py-4">
                        <div className="flex gap-2">
                          <Button
                            variant="secondary"
                            onClick={() => {
                              setSelectedService(service);
                              setModalOpen(true);
                            }}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button variant="danger" onClick={() => handleDelete(service)}>
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && services.length === 0 && (
            <EmptyState
              icon={Server}
              title="Aucun service monitoré"
              description="Les services alimentent les métriques et graphiques. Redémarrez le backend en dev pour charger les données démo, ou créez un service manuellement."
            />
          )}
        </CardBody>
      </Card>

      {isAdmin && (
        <ServiceFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadServices}
          service={selectedService}
        />
      )}
    </>
  );
}
