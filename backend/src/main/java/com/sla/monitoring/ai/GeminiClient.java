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
                    "Clé API Gemini non configurée. Définissez la variable d'environnement GEMINI_API_KEY.");
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
                    throw new BusinessException("Réponse Gemini vide ou bloquée : " + reason);
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
            throw new BusinessException("Appel Gemini interrompu");
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
            return """
                    Impossible de résoudre le DNS Gemini depuis le conteneur backend.
                    Ajoutez dns: [8.8.8.8, 1.1.1.1] au service backend (déjà dans docker-compose.yml),
                    puis relancez : docker compose up -d --force-recreate backend
                    """.trim();
        }

        if (lower.contains("timed out") || lower.contains("timeout")) {
            return "Délai d'attente dépassé lors de l'appel à Gemini. Vérifiez votre connexion Internet / proxy.";
        }

        if (lower.contains("connection refused") || lower.contains("network is unreachable")) {
            return "Réseau inaccessible vers Gemini. Vérifiez Docker Desktop → Settings → Network / DNS.";
        }

        return "Impossible de contacter Gemini : " + details;
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
            return "Erreur lors de l'appel à Gemini";
        }

        int status = ex.getStatusCode().value();
        String details = extractErrorMessage(ex.getResponseBodyAsString());

        if (status == 429) {
            return """
                    Quota Gemini dépassé ou indisponible (429).
                    Vérifiez que GEMINI_MODEL=gemini-2.5-flash (gemini-2.0-flash est déprécié).
                    Attendez quelques minutes ou consultez https://aistudio.google.com/apikey
                    """.trim() + (details.isBlank() ? "" : " — " + details);
        }
        if (status == 401 || status == 403) {
            return "Clé API Gemini invalide ou non autorisée. Régénérez une clé sur https://aistudio.google.com/apikey"
                    + (details.isBlank() ? "" : " — " + details);
        }
        if (status == 404) {
            return "Modèle Gemini introuvable : " + properties.getModel()
                    + ". Essayez GEMINI_MODEL=gemini-2.5-flash";
        }

        return "Erreur lors de l'appel à Gemini : " + status
                + (details.isBlank() ? "" : " — " + details);
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        try {
            JsonNode error = objectMapper.readTree(responseBody).path("error");
            String message = error.path("message").asText("");
            String status = error.path("status").asText("");
            if (!message.isBlank() && !status.isBlank()) {
                return status + ": " + message;
            }
            return message.isBlank() ? status : message;
        } catch (Exception ignored) {
            return "";
        }
    }
}
