"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { ClientFormModal } from "@/components/forms/ClientFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { ApiError, apiFetch } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Client } from "@/types";

export default function ClientsPage() {
  const { isAdmin } = useAuth();
  const router = useRouter();
  const [clients, setClients] = useState<Client[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);

  const loadClients = useCallback(() => {
    setLoading(true);
    setError(null);
    apiFetch<Client[]>("/api/clients")
      .then(setClients)
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
    loadClients();
  }, [isAdmin, router, loadClients]);

  async function handleDelete(client: Client) {
    if (!confirm(`Supprimer le client "${client.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/clients/${client.id}`, { method: "DELETE" });
      loadClients();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  if (!isAdmin) return null;

  return (
    <>
      <Header
        title="Clients"
        description="Gestion des clients associés aux contrats SLA."
        action={
          <Button
            onClick={() => {
              setSelectedClient(null);
              setModalOpen(true);
            }}
          >
            <Plus className="h-4 w-4" />
            Nouveau client
          </Button>
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadClients} />}

      <Card>
        <CardHeader
          title="Liste des clients"
          description={`${clients.length} client(s) enregistré(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
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
                {clients.map((client) => (
                  <tr key={client.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">{client.name}</td>
                    <td className="px-6 py-4 text-body">{client.email}</td>
                    <td className="px-6 py-4 text-body">{client.projectName || "—"}</td>
                    <td className="px-6 py-4 text-muted">{formatDate(client.createdAt)}</td>
                    <td className="px-6 py-4">
                      <div className="flex gap-2">
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
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && clients.length === 0 && (
            <div className="px-6 py-10 text-sm text-muted">Aucun client trouvé.</div>
          )}
        </CardBody>
      </Card>

      <ClientFormModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSaved={loadClients}
        client={selectedClient}
      />
    </>
  );
}
