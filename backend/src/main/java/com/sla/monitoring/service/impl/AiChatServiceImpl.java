package com.sla.monitoring.service.impl;

import com.sla.monitoring.ai.GeminiClient;
import com.sla.monitoring.dto.request.AiChatMessageRequest;
import com.sla.monitoring.dto.request.AiChatRequest;
import com.sla.monitoring.dto.response.AiChatResponse;
import com.sla.monitoring.service.AiChatService;
import com.sla.monitoring.service.AiContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private static final String SYSTEM_PROMPT = """
            Tu es l'assistant IA de SLA Monitor, une plateforme de suivi des SLA, incidents, alertes et projets.
            Réponds en français de manière claire, concise et professionnelle.
            Base tes réponses UNIQUEMENT sur le contexte applicatif fourni.
            Si la question sort du périmètre des données ou si l'information manque, dis-le explicitement.

            Règles de mise en forme OBLIGATOIRES :
            - Utilise un saut de ligne entre chaque élément d'une liste.
            - Pour lister des SLA, incidents, alertes, projets ou services, utilise UNE puce par ligne.
            - Format recommandé pour chaque élément :
              • **Nom** (#id) — Statut : VALEUR — Client : NOM
            - N'utilise JAMAIS le format brut du contexte avec des pipes (ex: "SLA #1 | nom | statut=").
            - N'enchaîne JAMAIS plusieurs éléments sur la même ligne.
            - Commence par une courte phrase d'introduction, puis la liste.
            - Exemple de bonne réponse :

            Voici les SLA disponibles :

            • **Production API SLA** (#1) — Statut : WARNING — Client : Acme Corp
            • **Payment Gateway SLA** (#2) — Statut : WARNING — Client : FinServ
            """;

    private final GeminiClient geminiClient;
    private final AiContextService aiContextService;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String context = aiContextService.buildApplicationContext();
        String historyBlock = formatHistory(request.getHistory());
        String userPrompt = """
                Contexte applicatif (données réelles accessibles à l'utilisateur) :
                %s

                Historique récent de la conversation :
                %s

                Question de l'utilisateur :
                %s
                """.formatted(context, historyBlock, request.getMessage().trim());

        String reply = geminiClient.generateText(SYSTEM_PROMPT, userPrompt);
        return AiChatResponse.builder().reply(reply).build();
    }

    private String formatHistory(List<AiChatMessageRequest> history) {
        if (history == null || history.isEmpty()) {
            return "Aucun historique.";
        }
        return history.stream()
                .limit(10)
                .map(message -> message.getRole() + " : " + message.getContent())
                .collect(Collectors.joining("\n"));
    }
}
