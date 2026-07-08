import type { Alert, Incident, Project, Report, Sla } from "@/types";

export interface ProjectSummary {
  project: Project;
  sla?: Sla;
  clientSlas: Sla[];
  alerts: Alert[];
  openAlerts: Alert[];
  incidents: Incident[];
  openIncidents: Incident[];
  reports: Report[];
}

export function buildProjectSummaries(
  projects: Project[],
  slas: Sla[],
  alerts: Alert[],
  incidents: Incident[],
  reports: Report[],
): ProjectSummary[] {
  return projects.map((project) => {
    const clientSlas = slas.filter((sla) => sla.clientId === project.clientId);
    const slaIds = new Set<number>(
      [
        project.slaId,
        ...clientSlas.map((item) => item.id),
      ].filter((id): id is number => id != null),
    );

    const sla =
      (project.slaId != null ? slas.find((item) => item.id === project.slaId) : undefined)
      ?? clientSlas[0];

    const projectAlerts = alerts.filter((alert) => slaIds.has(alert.slaId));
    const projectIncidents = incidents.filter(
      (incident) =>
        incident.projectId === project.id
        || (incident.projectId == null && incident.slaId != null && slaIds.has(incident.slaId)),
    );
    const projectReports = reports.filter(
      (report) => report.slaId != null && slaIds.has(report.slaId),
    );

    return {
      project,
      sla,
      clientSlas,
      alerts: projectAlerts,
      openAlerts: projectAlerts.filter((alert) => alert.status === "NEW"),
      incidents: projectIncidents,
      openIncidents: projectIncidents.filter((incident) => incident.status !== "RESOLVED"),
      reports: projectReports,
    };
  });
}
