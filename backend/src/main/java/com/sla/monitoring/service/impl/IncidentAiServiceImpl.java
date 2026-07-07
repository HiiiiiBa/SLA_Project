package com.sla.monitoring.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sla.monitoring.ai.GeminiClient;
import com.sla.monitoring.dto.response.IncidentAnalysisResponse;
import com.sla.monitoring.dto.response.IncidentCommentResponse;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.service.IncidentAiService;
import com.sla.monitoring.service.IncidentCommentService;
import com.sla.monitoring.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentAiServiceImpl implements IncidentAiService {

    private static final String SYSTEM_PROMPT = """
            Tu es un expert en gestion d'incidents et SLA pour une plateforme de monitoring.
            Analyse l'incident fourni et réponds UNIQUEMENT en JSON valide avec cette structure exacte :
            {
              "summary": "résumé concis en français",
              "probableCause": "cause probable en français",
              "businessImpact": "impact métier en français",
              "estimatedPriority": "Low | Medium | High | Critical",
              "recommendedSteps": ["étape 1", "étape 2", "étape 3"]
            }
            estimatedPriority doit être exactement Low, Medium, High ou Critical.
            recommendedSteps doit contenir entre 3 et 6 étapes actionnables en français.
            """;

    private final IncidentService incidentService;
    private final IncidentCommentService incidentCommentService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public IncidentAnalysisResponse analyzeIncident(Long incidentId) {
        IncidentResponse incident = incidentService.findById(incidentId);
        List<IncidentCommentResponse> comments = incidentCommentService.findByIncidentId(incidentId);

        String userPrompt = buildIncidentPrompt(incident, comments);
        String json = geminiClient.generateJson(SYSTEM_PROMPT, userPrompt);

        try {
            IncidentAnalysisResponse analysis = objectMapper.readValue(json, IncidentAnalysisResponse.class);
            validateAnalysis(analysis);
            return analysis;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Impossible d'interpréter l'analyse Gemini : " + ex.getMessage());
        }
    }

    private String buildIncidentPrompt(IncidentResponse incident, List<IncidentCommentResponse> comments) {
        String commentsBlock = comments.isEmpty()
                ? "Aucun commentaire."
                : comments.stream()
                        .map(comment -> "- " + comment.getAuthorName() + " : " + comment.getContent())
                        .collect(Collectors.joining("\n"));

        return """
                Incident #%d
                Statut : %s
                Sévérité déclarée : %s
                SLA : #%d
                Projet : %s
                Assigné à : %s
                Début : %s
                Fin : %s
                Description : %s

                Commentaires :
                %s
                """.formatted(
                incident.getId(),
                incident.getStatus(),
                incident.getSeverity(),
                incident.getSlaId(),
                incident.getProjectName() != null ? incident.getProjectName() : "Non renseigné",
                incident.getAssigneeName() != null ? incident.getAssigneeName() : "Non assigné",
                incident.getStartTime(),
                incident.getEndTime() != null ? incident.getEndTime() : "En cours",
                incident.getDescription(),
                commentsBlock);
    }

    private void validateAnalysis(IncidentAnalysisResponse analysis) {
        if (analysis.getSummary() == null || analysis.getSummary().isBlank()) {
            throw new BusinessException("Analyse Gemini incomplète : résumé manquant");
        }
        if (analysis.getRecommendedSteps() == null || analysis.getRecommendedSteps().isEmpty()) {
            throw new BusinessException("Analyse Gemini incomplète : étapes de résolution manquantes");
        }
    }
}
