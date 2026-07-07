package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.dto.response.ProjectResponse;
import com.sla.monitoring.dto.response.ServiceEntityResponse;
import com.sla.monitoring.dto.response.SlaResponse;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.security.util.SecurityUtils;
import com.sla.monitoring.service.AiContextService;
import com.sla.monitoring.service.AlertService;
import com.sla.monitoring.service.IncidentService;
import com.sla.monitoring.service.ProjectService;
import com.sla.monitoring.service.ServiceEntityService;
import com.sla.monitoring.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiContextServiceImpl implements AiContextService {

    private static final int MAX_ITEMS_PER_SECTION = 12;

    private final SlaService slaService;
    private final ProjectService projectService;
    private final IncidentService incidentService;
    private final AlertService alertService;
    private final ServiceEntityService serviceEntityService;

    @Override
    public String buildApplicationContext() {
        Role role = SecurityUtils.getCurrentUserDetails().getUser().getRole();
        StringBuilder context = new StringBuilder();
        context.append("Utilisateur connecté : rôle ").append(role).append("\n\n");

        appendSlas(context, slaService.getAll(null));
        appendProjects(context, projectService.findAll());
        appendServices(context, serviceEntityService.findAll(null));
        appendIncidents(context, incidentService.findAll());
        appendAlerts(context, alertService.findAll());

        context.append("\nRépondez uniquement à partir de ces données. ");
        context.append("Si l'information n'est pas disponible, indiquez-le clairement.\n");
        return context.toString();
    }

    private void appendSlas(StringBuilder context, List<SlaResponse> slas) {
        context.append("=== SLA (").append(slas.size()).append(") ===\n");
        slas.stream().limit(MAX_ITEMS_PER_SECTION).forEach(sla -> context
                .append("SLA #").append(sla.getId()).append(":\n")
                .append("  nom: ").append(sla.getName()).append("\n")
                .append("  statut: ").append(sla.getStatus()).append("\n")
                .append("  client: ").append(sla.getClientName() != null ? sla.getClientName() : sla.getClientId()).append("\n")
                .append("  uptime cible: ").append(sla.getUptimeTarget()).append("%\n")
                .append("  temps réponse max: ").append(sla.getResponseTimeLimit()).append(" ms\n")
                .append("  taux erreur max: ").append(sla.getErrorRateLimit()).append("%\n\n"));
        if (slas.size() > MAX_ITEMS_PER_SECTION) {
            context.append("... ").append(slas.size() - MAX_ITEMS_PER_SECTION).append(" SLA supplémentaires\n");
        }
        context.append("\n");
    }

    private void appendProjects(StringBuilder context, List<ProjectResponse> projects) {
        context.append("=== PROJETS (").append(projects.size()).append(") ===\n");
        projects.stream().limit(MAX_ITEMS_PER_SECTION).forEach(project -> context
                .append("Projet #").append(project.getId()).append(":\n")
                .append("  nom: ").append(project.getName()).append("\n")
                .append("  statut: ").append(project.getStatus()).append("\n")
                .append("  client: ").append(project.getClientName()).append("\n")
                .append("  SLA: ").append(project.getSlaName() != null ? project.getSlaName() : "—").append("\n")
                .append("  membres: ").append(project.getMemberCount()).append("\n\n"));
        if (projects.size() > MAX_ITEMS_PER_SECTION) {
            context.append("... ").append(projects.size() - MAX_ITEMS_PER_SECTION).append(" projets supplémentaires\n");
        }
        context.append("\n");
    }

    private void appendServices(StringBuilder context, List<ServiceEntityResponse> services) {
        context.append("=== SERVICES (").append(services.size()).append(") ===\n");
        services.stream().limit(MAX_ITEMS_PER_SECTION).forEach(service -> context
                .append("Service #").append(service.getId()).append(":\n")
                .append("  nom: ").append(service.getName()).append("\n")
                .append("  statut: ").append(service.getStatus()).append("\n")
                .append("  SLA: ").append(service.getSlaName() != null ? service.getSlaName() : service.getSlaId()).append("\n\n"));
        if (services.size() > MAX_ITEMS_PER_SECTION) {
            context.append("... ").append(services.size() - MAX_ITEMS_PER_SECTION).append(" services supplémentaires\n");
        }
        context.append("\n");
    }

    private void appendIncidents(StringBuilder context, List<IncidentResponse> incidents) {
        context.append("=== INCIDENTS (").append(incidents.size()).append(") ===\n");
        incidents.stream().limit(MAX_ITEMS_PER_SECTION).forEach(incident -> context
                .append("Incident #").append(incident.getId()).append(":\n")
                .append("  statut: ").append(incident.getStatus()).append("\n")
                .append("  sévérité: ").append(incident.getSeverity()).append("\n")
                .append("  SLA: #").append(incident.getSlaId()).append("\n")
                .append("  projet: ").append(incident.getProjectName() != null ? incident.getProjectName() : "—").append("\n")
                .append("  assigné: ").append(incident.getAssigneeName() != null ? incident.getAssigneeName() : "—").append("\n")
                .append("  description: ").append(truncate(incident.getDescription(), 120)).append("\n\n"));
        if (incidents.size() > MAX_ITEMS_PER_SECTION) {
            context.append("... ").append(incidents.size() - MAX_ITEMS_PER_SECTION).append(" incidents supplémentaires\n");
        }
        context.append("\n");
    }

    private void appendAlerts(StringBuilder context, List<AlertResponse> alerts) {
        context.append("=== ALERTES (").append(alerts.size()).append(") ===\n");
        alerts.stream().limit(MAX_ITEMS_PER_SECTION).forEach(alert -> context
                .append("Alerte #").append(alert.getId()).append(":\n")
                .append("  type: ").append(alert.getType()).append("\n")
                .append("  statut: ").append(alert.getStatus()).append("\n")
                .append("  SLA: ").append(alert.getSlaName() != null ? alert.getSlaName() : alert.getSlaId()).append("\n")
                .append("  service: ").append(alert.getServiceName() != null ? alert.getServiceName() : "—").append("\n")
                .append("  message: ").append(truncate(alert.getMessage(), 100)).append("\n\n"));
        if (alerts.size() > MAX_ITEMS_PER_SECTION) {
            context.append("... ").append(alerts.size() - MAX_ITEMS_PER_SECTION).append(" alertes supplémentaires\n");
        }
        context.append("\n");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\n', ' ').trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }
}
