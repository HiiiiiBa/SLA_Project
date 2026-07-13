package com.sla.monitoring.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sla.monitoring.ai.GeminiClient;
import com.sla.monitoring.dto.request.ExecutiveReportRequest;
import com.sla.monitoring.dto.response.ExecutiveReportKpiSummary;
import com.sla.monitoring.dto.response.ExecutiveReportListItemResponse;
import com.sla.monitoring.dto.response.ExecutiveReportResponse;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.ExecutiveReport;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.report.ExecutivePdfReportGenerator;
import com.sla.monitoring.report.ExecutiveReportDataLoader;
import com.sla.monitoring.report.model.ExecutiveReportContext;
import com.sla.monitoring.report.model.ReportExportResult;
import com.sla.monitoring.repository.ExecutiveReportRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ExecutiveReportAiService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExecutiveReportAiServiceImpl implements ExecutiveReportAiService {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String SYSTEM_PROMPT = """
            Tu es un analyste senior SLA / ITSM rédigeant un Executive Report pour la direction.
            Réponds UNIQUEMENT en JSON valide avec exactement cette structure :
            {
              "executiveSummary": "synthèse exécutive en français (2-4 phrases)",
              "kpiAnalysis": "analyse des KPI et conformité SLA en français",
              "incidentAnalysis": "analyse des incidents et alertes en français",
              "performanceTrends": "tendances de performance observées sur la période en français",
              "recommendations": ["recommandation actionnable 1", "recommandation 2", "recommandation 3"],
              "overallConclusion": "conclusion globale et niveau de risque en français"
            }
            Règles :
            - Français professionnel, clair et factuel.
            - Appuie-toi UNIQUEMENT sur les données fournies ; n'invente pas de chiffres.
            - recommendations : entre 3 et 6 items concrets.
            - Mentionne les écarts par rapport aux cibles SLA quand ils existent.
            """;

    private final ExecutiveReportDataLoader dataLoader;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final ExecutivePdfReportGenerator pdfReportGenerator;
    private final ExecutiveReportRepository executiveReportRepository;
    private final UserRepository userRepository;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    @Transactional
    public ExecutiveReportResponse generate(ExecutiveReportRequest request) {
        ExecutiveReportContext context = dataLoader.load(
                request.getProjectId(), request.getPeriodStart(), request.getPeriodEnd());

        String json = geminiClient.generateJson(SYSTEM_PROMPT, buildUserPrompt(context));
        AiNarrative narrative = parseNarrative(json);
        ExecutiveReportResponse response = toResponse(context, narrative, null, null);

        ExecutiveReport saved = persist(context, response, narrative);
        response.setId(saved.getId());
        response.setGeneratedByName(formatUserName(saved.getGeneratedBy()));
        return response;
    }

    @Override
    public List<ExecutiveReportListItemResponse> findAll(Long projectId) {
        if (projectId != null) {
            assertProjectAccess(projectId);
            return executiveReportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId).stream()
                    .map(this::toListItem)
                    .toList();
        }

        List<ExecutiveReport> reports;
        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> projectIds = employeeScopeService.getAssignedProjectIds();
            reports = projectIds.isEmpty()
                    ? List.of()
                    : executiveReportRepository.findByProjectIdInOrderByGeneratedAtDesc(projectIds);
        } else if (managerScopeService.isCurrentUserManager()) {
            Set<Long> projectIds = managerScopeService.getScopedProjectIds();
            reports = projectIds.isEmpty()
                    ? List.of()
                    : executiveReportRepository.findByProjectIdInOrderByGeneratedAtDesc(projectIds);
        } else if (clientScopeService.isCurrentUserClient()) {
            Set<Long> projectIds = clientScopeService.getScopedProjectIds();
            reports = projectIds.isEmpty()
                    ? List.of()
                    : executiveReportRepository.findByProjectIdInOrderByGeneratedAtDesc(projectIds);
        } else {
            reports = executiveReportRepository.findAllOrderByGeneratedAtDesc();
        }

        return reports.stream().map(this::toListItem).toList();
    }

    @Override
    public ExecutiveReportResponse findById(Long id) {
        ExecutiveReport entity = findEntity(id);
        assertProjectAccess(entity.getProject().getId());
        return fromEntity(entity);
    }

    @Override
    public ReportExportResult exportPdfById(Long id) {
        return exportPdf(findById(id));
    }

    @Override
    public ReportExportResult exportPdf(ExecutiveReportResponse report) {
        if (report == null || report.getProjectId() == null) {
            throw new BusinessException("Rapport exécutif invalide pour l'export PDF");
        }
        assertProjectAccess(report.getProjectId());
        return pdfReportGenerator.generate(report);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (SecurityUtils.getCurrentUserDetails().getUser().getRole() != Role.ADMIN) {
            throw new ForbiddenException("Seul un administrateur peut supprimer un rapport IA");
        }
        ExecutiveReport entity = findEntity(id);
        executiveReportRepository.delete(entity);
    }

    private ExecutiveReport persist(ExecutiveReportContext context,
                                    ExecutiveReportResponse response,
                                    AiNarrative narrative) {
        try {
            User generatedBy = userRepository.findById(SecurityUtils.getCurrentUserId()).orElse(null);
            Map<String, Object> narrativeMap = new HashMap<>();
            narrativeMap.put("executiveSummary", narrative.executiveSummary());
            narrativeMap.put("kpiAnalysis", narrative.kpiAnalysis());
            narrativeMap.put("incidentAnalysis", narrative.incidentAnalysis());
            narrativeMap.put("performanceTrends", narrative.performanceTrends());
            narrativeMap.put("recommendations", narrative.recommendations());
            narrativeMap.put("overallConclusion", narrative.overallConclusion());

            ExecutiveReport entity = ExecutiveReport.builder()
                    .project(context.getProject())
                    .sla(context.getSla())
                    .projectName(response.getProjectName())
                    .clientName(response.getClientName())
                    .slaName(response.getSlaName())
                    .periodStart(response.getPeriodStart())
                    .periodEnd(response.getPeriodEnd())
                    .generatedAt(response.getGeneratedAt())
                    .generatedBy(generatedBy)
                    .kpiSummary(objectMapper.writeValueAsString(response.getKpiSummary()))
                    .narrative(objectMapper.writeValueAsString(narrativeMap))
                    .build();

            return executiveReportRepository.save(entity);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Impossible d'enregistrer le rapport IA : " + ex.getMessage());
        }
    }

    private ExecutiveReport findEntity(Long id) {
        return executiveReportRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExecutiveReport", "id", id));
    }

    private void assertProjectAccess(Long projectId) {
        employeeScopeService.assertProjectAccess(projectId);
        managerScopeService.assertProjectAccess(projectId);
        clientScopeService.assertProjectAccess(projectId);
    }

    private ExecutiveReportListItemResponse toListItem(ExecutiveReport entity) {
        ExecutiveReportKpiSummary kpi = readKpi(entity.getKpiSummary());
        return ExecutiveReportListItemResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProjectName())
                .clientName(entity.getClientName())
                .slaId(entity.getSla() != null ? entity.getSla().getId() : null)
                .slaName(entity.getSlaName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .generatedAt(entity.getGeneratedAt())
                .slaScore(kpi != null ? kpi.getSlaScore() : null)
                .slaStatus(kpi != null ? kpi.getSlaStatus() : null)
                .incidentCount(kpi != null ? kpi.getIncidentCount() : null)
                .alertCount(kpi != null ? kpi.getAlertCount() : null)
                .generatedByName(formatUserName(entity.getGeneratedBy()))
                .build();
    }

    private ExecutiveReportResponse fromEntity(ExecutiveReport entity) {
        ExecutiveReportKpiSummary kpi = readKpi(entity.getKpiSummary());
        Map<String, Object> narrative = readNarrative(entity.getNarrative());

        @SuppressWarnings("unchecked")
        List<String> recommendations = narrative.get("recommendations") instanceof List<?> list
                ? list.stream().map(String::valueOf).toList()
                : List.of();

        return ExecutiveReportResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProjectName())
                .clientName(entity.getClientName())
                .slaId(entity.getSla() != null ? entity.getSla().getId() : null)
                .slaName(entity.getSlaName())
                .periodStart(entity.getPeriodStart())
                .periodEnd(entity.getPeriodEnd())
                .generatedAt(entity.getGeneratedAt())
                .generatedByName(formatUserName(entity.getGeneratedBy()))
                .kpiSummary(kpi)
                .executiveSummary(stringValue(narrative.get("executiveSummary")))
                .kpiAnalysis(stringValue(narrative.get("kpiAnalysis")))
                .incidentAnalysis(stringValue(narrative.get("incidentAnalysis")))
                .performanceTrends(stringValue(narrative.get("performanceTrends")))
                .recommendations(recommendations)
                .overallConclusion(stringValue(narrative.get("overallConclusion")))
                .build();
    }

    private ExecutiveReportResponse toResponse(ExecutiveReportContext context,
                                               AiNarrative narrative,
                                               Long id,
                                               String generatedByName) {
        return ExecutiveReportResponse.builder()
                .id(id)
                .projectId(context.getProject().getId())
                .projectName(context.getProject().getName())
                .clientName(context.getProject().getClient().getName())
                .slaId(context.getSla().getId())
                .slaName(context.getSla().getName())
                .periodStart(context.getPeriodStart())
                .periodEnd(context.getPeriodEnd())
                .generatedAt(LocalDateTime.now())
                .generatedByName(generatedByName)
                .kpiSummary(context.getKpiSummary())
                .executiveSummary(narrative.executiveSummary())
                .kpiAnalysis(narrative.kpiAnalysis())
                .incidentAnalysis(narrative.incidentAnalysis())
                .performanceTrends(narrative.performanceTrends())
                .recommendations(narrative.recommendations())
                .overallConclusion(narrative.overallConclusion())
                .build();
    }

    private ExecutiveReportKpiSummary readKpi(String json) {
        try {
            return objectMapper.readValue(json, ExecutiveReportKpiSummary.class);
        } catch (Exception ex) {
            throw new BusinessException("KPI du rapport IA illisibles : " + ex.getMessage());
        }
    }

    private Map<String, Object> readNarrative(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new BusinessException("Narratif du rapport IA illisible : " + ex.getMessage());
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String formatUserName(User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private String buildUserPrompt(ExecutiveReportContext context) {
        ExecutiveReportKpiSummary kpi = context.getKpiSummary();

        String incidentsBlock = context.getIncidents().isEmpty()
                ? "Aucun incident sur la période."
                : context.getIncidents().stream()
                        .limit(12)
                        .map(this::formatIncident)
                        .collect(Collectors.joining("\n"));

        String alertsBlock = context.getAlerts().isEmpty()
                ? "Aucune alerte sur la période."
                : context.getAlerts().stream()
                        .limit(12)
                        .map(this::formatAlert)
                        .collect(Collectors.joining("\n"));

        String servicesBlock = context.getServices().isEmpty()
                ? "Aucun service rattaché."
                : context.getServices().stream()
                        .map(this::formatService)
                        .collect(Collectors.joining("\n"));

        return """
                Projet : %s (id=%d)
                Client : %s
                SLA : %s (id=%d)
                Période : %s → %s

                === KPI consolidés ===
                Score SLA : %.1f / 100
                Statut SLA calculé : %s
                Disponibilité : %.2f%% (cible %.2f%%)
                Temps de réponse moyen : %.1f ms (limite %s ms)
                Conformité temps de réponse : %.1f%%
                Taux d'erreur moyen : %.2f%% (limite %.2f%%)
                Incidents : %d (dont critiques : %d)
                Alertes : %d
                Services DOWN : %d
                Services dégradés (approx.) : %d
                Métriques analysées : %d

                === Services ===
                %s

                === Incidents (échantillon) ===
                %s

                === Alertes (échantillon) ===
                %s
                """.formatted(
                context.getProject().getName(),
                context.getProject().getId(),
                context.getProject().getClient().getName(),
                context.getSla().getName(),
                context.getSla().getId(),
                DATE_TIME.format(context.getPeriodStart()),
                DATE_TIME.format(context.getPeriodEnd()),
                kpi.getSlaScore(),
                kpi.getSlaStatus(),
                kpi.getUptimePercentage(),
                kpi.getUptimeTarget(),
                kpi.getAverageResponseTime(),
                kpi.getResponseTimeLimit() != null ? String.valueOf(kpi.getResponseTimeLimit().intValue()) : "N/A",
                kpi.getResponseTimeCompliance(),
                kpi.getAverageErrorRate(),
                kpi.getErrorRateLimit() != null ? kpi.getErrorRateLimit() : 0.0,
                kpi.getIncidentCount(),
                kpi.getCriticalIncidentCount(),
                kpi.getAlertCount(),
                kpi.getServicesDown(),
                kpi.getServicesDegraded(),
                kpi.getMetricsAnalyzed(),
                servicesBlock,
                incidentsBlock,
                alertsBlock);
    }

    private String formatIncident(Incident incident) {
        return "- #%d | %s | %s | projet=%s | %s → %s | %s".formatted(
                incident.getId(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getProject() != null ? incident.getProject().getName() : "N/A",
                DATE_TIME.format(incident.getStartTime()),
                incident.getEndTime() != null ? DATE_TIME.format(incident.getEndTime()) : "en cours",
                truncate(incident.getDescription(), 160));
    }

    private String formatAlert(Alert alert) {
        return "- #%d | %s | %s | %s | %s".formatted(
                alert.getId(),
                alert.getType(),
                alert.getStatus(),
                DATE_TIME.format(alert.getCreatedAt()),
                truncate(alert.getMessage(), 160));
    }

    private String formatService(com.sla.monitoring.entity.Service service) {
        return "- #%d | %s | statut=%s".formatted(service.getId(), service.getName(), service.getStatus());
    }

    private AiNarrative parseNarrative(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String executiveSummary = text(root, "executiveSummary");
            String kpiAnalysis = text(root, "kpiAnalysis");
            String incidentAnalysis = text(root, "incidentAnalysis");
            String performanceTrends = text(root, "performanceTrends");
            String overallConclusion = text(root, "overallConclusion");

            List<String> recommendations = new ArrayList<>();
            JsonNode recNode = root.path("recommendations");
            if (recNode.isArray()) {
                for (JsonNode item : recNode) {
                    String value = item.asText("").trim();
                    if (!value.isBlank()) {
                        recommendations.add(value);
                    }
                }
            }

            if (executiveSummary.isBlank() || overallConclusion.isBlank() || recommendations.isEmpty()) {
                throw new BusinessException("Rapport exécutif incomplet : sections obligatoires manquantes");
            }

            return new AiNarrative(
                    executiveSummary,
                    kpiAnalysis,
                    incidentAnalysis,
                    performanceTrends,
                    recommendations,
                    overallConclusion);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Impossible d'interpréter le rapport exécutif : " + ex.getMessage());
        }
    }

    private String text(JsonNode root, String field) {
        return root.path(field).asText("").trim();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('\n', ' ').trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max - 1) + "…";
    }

    private record AiNarrative(
            String executiveSummary,
            String kpiAnalysis,
            String incidentAnalysis,
            String performanceTrends,
            List<String> recommendations,
            String overallConclusion) {
    }
}
