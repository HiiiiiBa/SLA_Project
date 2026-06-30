"use client";

import { useEffect, useState } from "react";
import { Download, FileSpreadsheet, FileText } from "lucide-react";
import { Header } from "@/components/layout/Header";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorBanner } from "@/components/ui/ErrorBanner";
import { ApiError, apiFetch, downloadReport } from "@/lib/api";
import { formatDate, formatScore } from "@/lib/utils";
import type { Report } from "@/types";

export default function ReportsPage() {
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Report[]>("/api/reports")
      .then(setReports)
      .catch((err) =>
        setError(err instanceof ApiError ? err.message : "Erreur de chargement"),
      )
      .finally(() => setLoading(false));
  }, []);

  async function handleDownload(reportId: number, format: "pdf" | "csv") {
    setDownloadingId(`${reportId}-${format}`);
    try {
      await downloadReport(reportId, format);
    } finally {
      setDownloadingId(null);
    }
  }

  return (
    <>
      <Header
        title="Rapports"
        description="Consultez et exportez vos rapports SLA au format PDF ou CSV."
      />

      {error && <ErrorBanner message={error} />}

      <Card>
        <CardHeader
          title="Rapports générés"
          description={`${reports.length} rapport(s) disponible(s)`}
        />
        <CardBody className="overflow-x-auto p-0">
          {loading ? (
            <div className="px-6 py-10 text-sm text-slate-400">Chargement...</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50 text-left text-slate-500">
                <tr>
                  <th className="px-6 py-4 font-medium">ID</th>
                  <th className="px-6 py-4 font-medium">SLA</th>
                  <th className="px-6 py-4 font-medium">Score</th>
                  <th className="px-6 py-4 font-medium">Période</th>
                  <th className="px-6 py-4 font-medium">Format</th>
                  <th className="px-6 py-4 font-medium">Généré le</th>
                  <th className="px-6 py-4 font-medium">Export</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((report) => (
                  <tr key={report.id} className="border-t border-slate-100 hover:bg-slate-50/70">
                    <td className="px-6 py-4 text-slate-500">#{report.id}</td>
                    <td className="px-6 py-4 font-medium text-slate-900">
                      SLA #{report.slaId}
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {formatScore(report.slaResult)}
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {formatDate(report.periodStart)}
                      <span className="mx-1 text-slate-300">→</span>
                      {formatDate(report.periodEnd)}
                    </td>
                    <td className="px-6 py-4 text-slate-600">{report.format}</td>
                    <td className="px-6 py-4 text-slate-500">
                      {formatDate(report.generatedAt)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex gap-2">
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
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loading && reports.length === 0 && (
            <div className="flex flex-col items-center gap-3 px-6 py-14 text-center">
              <Download className="h-8 w-8 text-slate-300" />
              <p className="text-sm text-slate-400">
                Aucun rapport disponible. Lancez une évaluation SLA pour en générer.
              </p>
            </div>
          )}
        </CardBody>
      </Card>
    </>
  );
}
