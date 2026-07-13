package com.sla.monitoring.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sla.monitoring.config.GeminiProperties;
import com.sla.monitoring.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_DELAY_MS = 1500L;

    private final GeminiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public String generateText(String systemInstruction, String userPrompt) {
        return callGemini(systemInstruction, userPrompt, false);
    }

    public String generateJson(String systemInstruction, String userPrompt) {
        return callGemini(systemInstruction, userPrompt, true);
    }

    private void assertConfigured() {
        if (!properties.isEnabled()) {
            throw new BusinessException("L'assistant IA est désactivé");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BusinessException(
                    "Assistant IA non configuré. Contactez l'administrateur.");
        }
    }

    private String callGemini(String systemInstruction, String userPrompt, boolean jsonResponse) {
        assertConfigured();

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", jsonResponse ? 0.3 : 0.4);
        if (jsonResponse) {
            generationConfig.put("responseMimeType", "application/json");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))),
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", generationConfig);

        String url = properties.getBaseUrl()
                + "/models/" + properties.getModel() + ":generateContent";

        RestClientResponseException lastRateLimitError = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String responseBody = restClient.post()
                        .uri(url)
                        .header("x-goog-api-key", properties.getApiKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                    String reason = root.path("promptFeedback").path("blockReason").asText("unknown");
                    throw new BusinessException("Réponse de l'assistant IA vide ou bloquée");
                }
                return textNode.asText().trim();
            } catch (RestClientResponseException ex) {
                log.error("Gemini API error {} (attempt {}/{}): {}",
                        ex.getStatusCode(), attempt, MAX_RETRIES, ex.getResponseBodyAsString());

                if (ex.getStatusCode().value() == 429 && attempt < MAX_RETRIES) {
                    lastRateLimitError = ex;
                    sleepBeforeRetry(attempt);
                    continue;
                }

                throw new BusinessException(mapApiError(ex));
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Gemini call failed", ex);
                throw new BusinessException(mapConnectivityError(ex));
            }
        }

        throw new BusinessException(mapApiError(lastRateLimitError));
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(RETRY_BASE_DELAY_MS * attempt);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Appel à l'assistant IA interrompu");
        }
    }

    private String mapConnectivityError(Exception ex) {
        String details = rootCauseMessage(ex);
        String lower = details.toLowerCase();

        if (lower.contains("unknown host")
                || lower.contains("nodename nor servname")
                || lower.contains("name or service not known")
                || lower.contains("temporary failure in name resolution")
                || lower.contains("failed to resolve")) {
            return "Impossible de joindre l'assistant IA (problème réseau/DNS). Contactez l'administrateur.";
        }

        if (lower.contains("timed out") || lower.contains("timeout")) {
            return "Délai d'attente dépassé pour l'assistant IA. Réessayez plus tard.";
        }

        if (lower.contains("connection refused") || lower.contains("network is unreachable")) {
            return "Réseau inaccessible pour l'assistant IA. Contactez l'administrateur.";
        }

        return "Impossible de contacter l'assistant IA.";
    }

    private String rootCauseMessage(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    private String mapApiError(RestClientResponseException ex) {
        if (ex == null) {
            return "Erreur lors de l'appel à l'assistant IA";
        }

        int status = ex.getStatusCode().value();

        if (status == 429) {
            return "Quota de l'assistant IA temporairement dépassé. Réessayez dans quelques minutes.";
        }
        if (status == 401 || status == 403) {
            return "Assistant IA non autorisé. Contactez l'administrateur.";
        }
        if (status == 404) {
            return "Service d'assistant IA indisponible. Contactez l'administrateur.";
        }

        return "Erreur lors de l'appel à l'assistant IA.";
    }
}
