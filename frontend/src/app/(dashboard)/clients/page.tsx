"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Building2, Eye, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { ClientFormModal } from "@/components/forms/ClientFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Input } from "@/components/ui/Input";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Client } from "@/types";

export default function ClientsPage() {
  const { isAdmin, isManager, canViewClients } = useAuth();
  const sessionUserId = useSessionUserId();
  const router = useRouter();
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const filteredClients = useMemo(() => {
    const normalized = searchQuery.trim().toLowerCase();
    if (!normalized) return clients;
    return clients.filter(
      (client) =>
        client.name.toLowerCase().includes(normalized)
        || client.email.toLowerCase().includes(normalized)
        || client.projectName?.toLowerCase().includes(normalized),
    );
  }, [clients, searchQuery]);

  const loadClients = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    apiFetch<Client[]>("/api/clients")
      .then(setClients)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId]);

  useEffect(() => {
    if (!canViewClients) {
      router.replace("/dashboard");
      return;
    }
    loadClients();
  }, [canViewClients, router, loadClients, sessionUserId]);

  async function handleDelete(client: Client) {
    if (!confirm(`Supprimer le client "${client.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/clients/${client.id}`, { method: "DELETE" });
      loadClients();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  if (!canViewClients) return null;

  return (
    <>
      <Header
        title="Clients"
        description={
          isManager && !isAdmin
            ? "Clients qui vous sont affectés en tant que manager."
            : "Gestion des clients associés aux contrats SLA."
        }
        action={
          isAdmin ? (
            <Button
              onClick={() => {
                setSelectedClient(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouveau client
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadClients} />}

      <Card>
        <CardHeader
          title="Liste des clients"
          description={
            searchQuery.trim()
              ? `${filteredClients.length} sur ${clients.length} client(s)`
              : `${clients.length} client(s) enregistré(s)`
          }
        />
        <CardBody className="space-y-4">
          {!loading && clients.length > 0 && (
            <div className="relative max-w-md">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
              <Input
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                placeholder="Rechercher par nom, email ou projet..."
                className="pl-9"
              />
            </div>
          )}

          <div className="overflow-x-auto">
          {loading ? (
            <div className="px-2 py-10 text-sm text-muted">Chargement...</div>
          ) : clients.length === 0 ? (
            <EmptyState
              icon={Building2}
              title="Aucun client enregistré"
              description="Ajoutez un client pour lui associer des contrats SLA et des rapports."
            />
          ) : filteredClients.length === 0 ? (
            <EmptyState
              icon={Search}
              title="Aucun résultat"
              description={`Aucun client ne correspond à « ${searchQuery.trim()} ».`}
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Email</th>
                  <th className="px-6 py-4 font-medium">Projet</th>
                  <th className="px-6 py-4 font-medium">Créé le</th>
                  <th className="px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredClients.map((client) => (
                  <tr key={client.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{client.name}</td>
                    <td className="px-6 py-4 text-body">{client.email}</td>
                    <td className="px-6 py-4 text-body">{client.projectName || "—"}</td>
                    <td className="px-6 py-4 text-muted">{formatDate(client.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-2">
                        <Link href={`/clients/${client.id}`}>
                          <Button variant="secondary">
                            <Eye className="h-4 w-4" />
                          </Button>
                        </Link>
                        {isAdmin && (
                          <>
                            <Button
                              variant="secondary"
                              onClick={() => {
                                setSelectedClient(client);
                                setModalOpen(true);
                              }}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            <Button variant="danger" onClick={() => handleDelete(client)}>
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
          </div>
        </CardBody>
      </Card>

      {isAdmin && (
        <ClientFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadClients}
          client={selectedClient}
        />
      )}
    </>
  );
}
