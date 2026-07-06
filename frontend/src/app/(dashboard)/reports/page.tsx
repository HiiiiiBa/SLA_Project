"use client";

import { useCallback, useEffect, useState } from "react";
import { Download, FileSpreadsheet, FileText, Trash2 } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { useAuth } from "@/context/AuthContext";
import { useSessionUserId } from "@/hooks/useSessionUserId";
import { ApiError, apiFetch, downloadReport } from "@/lib/api";
import { formatDate, formatScore } from "@/lib/utils";
import type { Report } from "@/types";

export default function ReportsPage() {
  const { isAdmin, canDownloadReports } = useAuth();
  const sessionUserId = useSessionUserId();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

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

  useEffect(() => {
    loadReports();
  }, [loadReports]);

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

  return (
    <>
      <Header
        title="Rapports"
        description="Export PDF/CSV : statut SLA, uptime, incidents, alertes et performance globale."
      />

      {error && <ErrorBanner message={error} onRetry={loadReports} />}

      <Card>
        <CardHeader
          title="Rapports générés"
          description={`${reports.length} rapport(s) disponible(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-muted">Chargement...</div>
          ) : reports.length === 0 ? (
            <EmptyState
              icon={Download}
              title="Aucun rapport disponible"
              description="Lancez une simulation puis une évaluation SLA depuis Administration pour générer des rapports."
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
                {reports.map((report) => (
                  <tr key={report.id} className="table-row">
                    <td className="px-6 py-4 text-muted">#{report.id}</td>
                    <td className="px-6 py-4 font-medium text-heading">
                      SLA #{report.slaId}
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
    </>
  );
}
