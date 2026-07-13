"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Download,
  Eye,
  FileSpreadsheet,
  FileText,
  Sparkles,
  Trash2,
} from "lucide-react";
import { ExecutiveReportModal } from "@/components/reports/ExecutiveReportModal";
import { ExecutiveReportView } from "@/components/reports/ExecutiveReportView";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { Select } from "@/components/ui/Select";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import {
  ApiError,
  apiFetch,
  downloadExecutiveReportPdf,
  downloadExecutiveReportPdfById,
  downloadReport,
} from "@/lib/api";
import { formatDate, formatScore } from "@/lib/utils";
import type {
  ExecutiveReport,
  ExecutiveReportListItem,
  Report,
  Sla,
} from "@/types";

export default function ReportsPage() {
  const { isAdmin, isClient, isEmployee, canDownloadReports } = useAuth();
  const sessionUserId = useSessionUserId();
  const [reports, setReports] = useState<Report[]>([]);
  const [slas, setSlas] = useState<Sla[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [filterSlaId, setFilterSlaId] = useState("");
  const [filterClientId, setFilterClientId] = useState("");
  const [aiModalOpen, setAiModalOpen] = useState(false);
  const [executiveReport, setExecutiveReport] = useState<ExecutiveReport | null>(null);
  const [exportingExecutive, setExportingExecutive] = useState(false);
  const [aiHistory, setAiHistory] = useState<ExecutiveReportListItem[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [openingHistoryId, setOpeningHistoryId] = useState<number | null>(null);

  const loadReports = useCallback(() => {
    if (!sessionUserId) return;
    setLoading(true);
    setError(null);
    apiFetch<Report[]>("/api/reports")
      .then(setReports)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, [sessionUserId]);

  const loadAiHistory = useCallback(() => {
    if (!sessionUserId) return;
    setLoadingHistory(true);
    apiFetch<ExecutiveReportListItem[]>("/api/ai/executive-report")
      .then(setAiHistory)
      .catch(() => setAiHistory([]))
      .finally(() => setLoadingHistory(false));
  }, [sessionUserId]);

  useEffect(() => {
    loadReports();
  }, [loadReports]);

  useEffect(() => {
    loadAiHistory();
  }, [loadAiHistory]);

  useEffect(() => {
    if (!sessionUserId) return;
    apiFetch<Sla[]>("/api/slas")
      .then(setSlas)
      .catch(() => setSlas([]));
  }, [sessionUserId]);

  const slaById = useMemo(() => new Map(slas.map((sla) => [sla.id, sla])), [slas]);

  const clients = useMemo(() => {
    const map = new Map<number, string>();
    for (const sla of slas) {
      map.set(sla.clientId, sla.clientName ?? `Client #${sla.clientId}`);
    }
    return Array.from(map.entries()).sort(([, a], [, b]) => a.localeCompare(b));
  }, [slas]);

  const filteredSlas = useMemo(() => {
    if (!filterClientId) return slas;
    return slas.filter((sla) => String(sla.clientId) === filterClientId);
  }, [slas, filterClientId]);

  const filteredReports = useMemo(() => {
    return reports.filter((report) => {
      if (filterSlaId && String(report.slaId) !== filterSlaId) return false;
      if (filterClientId) {
        const sla = slaById.get(report.slaId);
        if (!sla || String(sla.clientId) !== filterClientId) return false;
      }
      return true;
    });
  }, [reports, filterSlaId, filterClientId, slaById]);

  const hasActiveFilters = Boolean(filterSlaId || filterClientId);

  function handleClientChange(clientId: string) {
    setFilterClientId(clientId);
    if (clientId && filterSlaId) {
      const sla = slaById.get(Number(filterSlaId));
      if (!sla || String(sla.clientId) !== clientId) {
        setFilterSlaId("");
      }
    }
  }

  function resetFilters() {
    setFilterSlaId("");
    setFilterClientId("");
  }

  async function handleDownload(reportId: number, format: "pdf" | "csv") {
    setDownloadingId(`${reportId}-${format}`);
    try {
      await downloadReport(reportId, format);
    } finally {
      setDownloadingId(null);
    }
  }

  async function handleDelete(report: Report) {
    if (!confirm(`Supprimer le rapport #${report.id} ?`)) return;
    try {
      await apiFetch<void>(`/api/reports/${report.id}`, { method: "DELETE" });
      loadReports();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  async function handleExportExecutivePdf() {
    if (!executiveReport) return;
    setExportingExecutive(true);
    setError(null);
    try {
      if (executiveReport.id) {
        await downloadExecutiveReportPdfById(executiveReport.id);
      } else {
        await downloadExecutiveReportPdf(executiveReport);
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Export PDF impossible");
    } finally {
      setExportingExecutive(false);
    }
  }

  async function handleOpenHistoryItem(id: number) {
    setOpeningHistoryId(id);
    setError(null);
    try {
      const report = await apiFetch<ExecutiveReport>(`/api/ai/executive-report/${id}`);
      setExecutiveReport(report);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Impossible d'ouvrir le rapport IA");
    } finally {
      setOpeningHistoryId(null);
    }
  }

  async function handleDownloadHistoryPdf(id: number) {
    setDownloadingId(`ai-${id}-pdf`);
    setError(null);
    try {
      await downloadExecutiveReportPdfById(id);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Export PDF impossible");
    } finally {
      setDownloadingId(null);
    }
  }

  async function handleDeleteHistoryItem(item: ExecutiveReportListItem) {
    if (!confirm(`Supprimer le rapport IA #${item.id} ?`)) return;
    try {
      await apiFetch<void>(`/api/ai/executive-report/${item.id}`, { method: "DELETE" });
      if (executiveReport?.id === item.id) {
        setExecutiveReport(null);
      }
      loadAiHistory();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Suppression impossible");
    }
  }

  function handleGenerated(report: ExecutiveReport) {
    setExecutiveReport(report);
    loadAiHistory();
  }

  return (
    <>
      <Header
        title="Rapports"
        description={
          isClient
            ? "Téléchargement des rapports PDF/CSV de vos SLA."
            : isEmployee
              ? "Rapports PDF/CSV des SLA liés à vos projets assignés."
              : "Export PDF/CSV : statut SLA, uptime, incidents, alertes et performance globale."
        }
        action={
          canDownloadReports ? (
            <Button onClick={() => setAiModalOpen(true)}>
              <Sparkles className="h-4 w-4" />
              Générer un rapport exécutif
            </Button>
          ) : undefined
        }
      />

      {error && <ErrorBanner message={error} onRetry={loadReports} />}

      {executiveReport && (
        <div className="mb-6">
          <ExecutiveReportView
            report={executiveReport}
            exporting={exportingExecutive}
            onExportPdf={handleExportExecutivePdf}
            onClose={() => setExecutiveReport(null)}
          />
        </div>
      )}

      <Card className="mb-6">
        <CardHeader
          title="Historique des rapports exécutifs"
          description={`${aiHistory.length} rapport(s) exécutif(s) sauvegardé(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loadingHistory ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : aiHistory.length === 0 ? (
            <EmptyState
              icon={Sparkles}
              title="Aucun rapport exécutif"
              description="Générez un rapport exécutif pour le retrouver ici."
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">Projet</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Score</th>
                  <th className="px-6 py-4 font-medium">Période</th>
                  <th className="px-6 py-4 font-medium">Généré le</th>
                  <th className="px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {aiHistory.map((item) => (
                  <tr key={item.id} className="table-row">
                    <td className="px-6 py-4 text-muted">#{item.id}</td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-heading">{item.projectName}</div>
                      <div className="text-xs text-muted">{item.clientName}</div>
                    </td>
                    <td className="px-6 py-4 text-body">{item.slaName}</td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-heading">
                        {item.slaScore != null ? formatScore(item.slaScore) : "—"}
                      </span>
                      {item.slaStatus && (
                        <div className="text-xs text-muted">{item.slaStatus}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-body">
                      {formatDate(item.periodStart)}
                      <span className="mx-1 text-muted">→</span>
                      {formatDate(item.periodEnd)}
                    </td>
                    <td className="px-6 py-4 text-muted">
                      <div>{formatDate(item.generatedAt)}</div>
                      {item.generatedByName && (
                        <div className="text-xs">{item.generatedByName}</div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-wrap gap-2">
                        <Button
                          variant="secondary"
                          loading={openingHistoryId === item.id}
                          onClick={() => handleOpenHistoryItem(item.id)}
                        >
                          <Eye className="h-4 w-4" />
                          Voir
                        </Button>
                        <Button
                          variant="secondary"
                          loading={downloadingId === `ai-${item.id}-pdf`}
                          onClick={() => handleDownloadHistoryPdf(item.id)}
                        >
                          <FileText className="h-4 w-4" />
                          PDF
                        </Button>
                        {isAdmin && (
                          <Button
                            variant="danger"
                            onClick={() => handleDeleteHistoryItem(item)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>

      <Card className="mb-6">
        <CardHeader
          title="Filtres"
          description="Filtrer les rapports SLA classiques par client ou SLA"
        />
        <CardBody className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            {!isClient && !isEmployee && (
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Client
                </label>
                <Select
                  value={filterClientId}
                  onChange={(event) => handleClientChange(event.target.value)}
                >
                  <option value="">Tous les clients</option>
                  {clients.map(([clientId, clientName]) => (
                    <option key={clientId} value={clientId}>
                      {clientName}
                    </option>
                  ))}
                </Select>
              </div>
            )}

            <div className="space-y-2">
              <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                SLA
              </label>
              <Select
                value={filterSlaId}
                onChange={(event) => setFilterSlaId(event.target.value)}
              >
                <option value="">Tous les SLA</option>
                {filteredSlas.map((sla) => (
                  <option key={sla.id} value={sla.id}>
                    {sla.name}
                    {sla.clientName ? ` (${sla.clientName})` : ""}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          {hasActiveFilters && (
            <div className="flex items-center justify-between gap-3 border-t border-border/60 pt-4">
              <p className="text-sm text-muted">
                {filteredReports.length} rapport(s) sur {reports.length}
              </p>
              <Button variant="secondary" onClick={resetFilters}>
                Réinitialiser les filtres
              </Button>
            </div>
          )}
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Rapports SLA générés"
          description={`${filteredReports.length} rapport(s) affiché(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : reports.length === 0 ? (
            <EmptyState
              icon={Download}
              title="Aucun rapport disponible"
              description={
                isEmployee
                  ? "Les rapports sont générés automatiquement pour les SLA de vos projets."
                  : "Lancez une simulation puis une évaluation SLA depuis Administration pour générer des rapports."
              }
            />
          ) : filteredReports.length === 0 ? (
            <EmptyState
              icon={Download}
              title="Aucun rapport ne correspond aux filtres"
              description="Modifiez ou réinitialisez les filtres pour afficher d'autres rapports."
            />
          ) : (
            <table className="min-w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Score</th>
                  <th className="px-6 py-4 font-medium">Période</th>
                  <th className="px-6 py-4 font-medium">Format</th>
                  <th className="px-6 py-4 font-medium">Généré le</th>
                  <th className="px-6 py-4 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredReports.map((report) => (
                  <tr key={report.id} className="table-row">
                    <td className="px-6 py-4 text-muted">#{report.id}</td>
                    <td className="px-6 py-4 font-medium text-heading">
                      {slaById.get(report.slaId)?.name ?? `SLA #${report.slaId}`}
                    </td>
                    <td className="px-6 py-4">
                      <span className="font-semibold text-heading">
                        {formatScore(report.slaResult)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-body">
                      {formatDate(report.periodStart)}
                      <span className="mx-1 text-muted">→</span>
                      {formatDate(report.periodEnd)}
                    </td>
                    <td className="px-6 py-4">
                      <span className="rounded-lg bg-card/60 px-2.5 py-1 text-xs font-semibold text-body ring-1 ring-border/60">
                        {report.format}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-muted">
                      {formatDate(report.generatedAt)}
                    </td>
                    <td className="px-6 py-4">
                      {canDownloadReports ? (
                        <div className="flex flex-wrap gap-2">
                          <Button
                            variant="secondary"
                            loading={downloadingId === `${report.id}-pdf`}
                            onClick={() => handleDownload(report.id, "pdf")}
                          >
                            <FileText className="h-4 w-4" />
                            PDF
                          </Button>
                          <Button
                            variant="secondary"
                            loading={downloadingId === `${report.id}-csv`}
                            onClick={() => handleDownload(report.id, "csv")}
                          >
                            <FileSpreadsheet className="h-4 w-4" />
                            CSV
                          </Button>
                          {isAdmin && (
                            <Button variant="danger" onClick={() => handleDelete(report)}>
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          )}
                        </div>
                      ) : (
                        <span className="text-xs text-muted">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>

      <ExecutiveReportModal
        open={aiModalOpen}
        onClose={() => setAiModalOpen(false)}
        onGenerated={handleGenerated}
      />
    </>
  );
}
