"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Eye, FolderKanban, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { RequestApprovalButton } from "@/components/approval/RequestApprovalButton";
import { ProjectFormModal } from "@/components/forms/ProjectFormModal";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Input } from "@/components/ui/Input";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch } from "@/lib/api";
import type { Project } from "@/types";

export default function ProjectsPage() {
  const { canManageOrg, isAdmin, canRequestApproval, isClient, isEmployee } = useAuth();
  const [success, setSuccess] = useState<string | null>(null);
  const sessionUserId = useSessionUserId();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const filteredProjects = useMemo(() => {
    const normalized = searchQuery.trim().toLowerCase();
    if (!normalized) return projects;
    return projects.filter(
      (project) =>
        project.name.toLowerCase().includes(normalized)
        || project.clientName.toLowerCase().includes(normalized)
        || project.teamName?.toLowerCase().includes(normalized)
        || project.managerName?.toLowerCase().includes(normalized)
        || project.slaName?.toLowerCase().includes(normalized)
        || project.status.toLowerCase().includes(normalized),
    );
  }, [projects, searchQuery]);

  const loadProjects = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    apiFetch<Project[]>("/api/projects")
      .then(setProjects)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId]);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  async function handleDelete(project: Project) {
    if (!confirm(`Supprimer le projet "${project.name}" ?`)) return;
    try {
      await apiFetch<void>(`/api/projects/${project.id}`, { method: "DELETE" });
      loadProjects();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  return (
    <>
      <Header
        title="Projets"
        description={
          isClient
            ? "Consultation de vos projets (lecture seule)."
            : isEmployee
              ? "Projets auxquels vous êtes assigné."
              : "Un client peut avoir plusieurs projets, chacun géré par une équipe et des employés."
        }
        action={
          canManageOrg ? (
            <Button
              onClick={() => {
                setSelectedProject(null);
                setModalOpen(true);
              }}
            >
              <Plus className="h-4 w-4" />
              Nouveau projet
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadProjects} />}
      {success && (
        <div className="mb-4 rounded-xl border border-success/30 bg-success/10 px-4 py-3 text-sm text-success">
          {success}
        </div>
      )}

      <Card>
        <CardHeader
          title="Liste des projets"
          description={
            searchQuery.trim()
              ? `${filteredProjects.length} sur ${projects.length} projet(s)`
              : `${projects.length} projet(s)`
          }
        />
        <CardBody className="space-y-4">
          {!loading && projects.length > 0 && (
            <div className="relative max-w-md">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
              <Input
                value={searchQuery}
                onChange={(event) => setSearchQuery(event.target.value)}
                placeholder="Rechercher par nom, client, équipe, manager..."
                className="pl-9"
              />
            </div>
          )}

          <div className="overflow-x-auto">
          {loading ? (
            <div className="px-2 py-10 text-sm text-muted">Chargement...</div>
          ) : projects.length === 0 ? (
            <EmptyState
              icon={FolderKanban}
              title="Aucun projet"
              description="Associez un projet à un client et à une équipe."
            />
          ) : filteredProjects.length === 0 ? (
            <EmptyState
              icon={Search}
              title="Aucun résultat"
              description={`Aucun projet ne correspond à « ${searchQuery.trim()} ».`}
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">Nom</th>
                  <th className="px-6 py-4 font-medium">Client</th>
                  <th className="px-6 py-4 font-medium">Équipe</th>
                  <th className="px-6 py-4 font-medium">Manager</th>
                  <th className="px-6 py-4 font-medium">Employés</th>
                  <th className="px-6 py-4 font-medium">Statut</th>
                  {canManageOrg && <th className="px-6 py-4 font-medium">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {filteredProjects.map((project) => (
                  <tr key={project.id} className="table-row">
                    <td className="px-6 py-4 font-medium text-heading">
                      <Link href={`/projects/${project.id}`} className="hover:text-primary">
                        {project.name}
                      </Link>
                    </td>
                    <td className="px-6 py-4 text-body">{project.clientName}</td>
                    <td className="px-6 py-4 text-body">{project.teamName ?? "—"}</td>
                    <td className="px-6 py-4 text-body">{project.managerName ?? "—"}</td>
                    <td className="px-6 py-4 text-body">{project.memberCount}</td>
                    <td className="px-6 py-4 text-body">{project.status}</td>
                    {canManageOrg && (
                      <td className="whitespace-nowrap px-6 py-4">
                        <div className="inline-flex items-center gap-1.5">
                          <Link href={`/projects/${project.id}`}>
                            <Button
                              variant="secondary"
                              className="!px-2.5 !py-2"
                              title="Voir"
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                          </Link>
                          <Button
                            variant="secondary"
                            className="!px-2.5 !py-2"
                            onClick={() => {
                              setSelectedProject(project);
                              setModalOpen(true);
                            }}
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          {isAdmin && (
                            <Button
                              variant="danger"
                              className="!px-2.5 !py-2"
                              onClick={() => handleDelete(project)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          )}
                          {canRequestApproval && (
                            <RequestApprovalButton
                              actionType="DELETE_PROJECT"
                              targetType="PROJECT"
                              targetId={project.id}
                              targetLabel={project.name}
                              confirmMessage={`Demander la suppression du projet "${project.name}" à l'admin ?`}
                              title="Demander la suppression (validation admin)"
                              className="!px-2.5 !py-2"
                              onSuccess={() => {
                                setSuccess(`Demande envoyée pour le projet "${project.name}".`);
                                setError(null);
                              }}
                              onError={setError}
                            />
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          </div>
        </CardBody>
      </Card>

      {canManageOrg && (
        <ProjectFormModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onSaved={loadProjects}
          project={selectedProject}
        />
      )}
    </>
  );
}
