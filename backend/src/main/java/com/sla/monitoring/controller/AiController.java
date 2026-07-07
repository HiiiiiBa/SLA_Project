package com.sla.monitoring.controller;

import com.sla.monitoring.dto.ApiResponse;
import com.sla.monitoring.dto.request.AiChatRequest;
import com.sla.monitoring.dto.response.AiChatResponse;
import com.sla.monitoring.dto.response.IncidentAnalysisResponse;
import com.sla.monitoring.service.AiChatService;
import com.sla.monitoring.service.IncidentAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI", description = "Gemini-powered incident analysis and chatbot")
public class AiController {

    private final IncidentAiService incidentAiService;
    private final AiChatService aiChatService;

    @PostMapping("/incidents/{id}/analyze")
    @Operation(summary = "Analyze an incident with Gemini AI")
    public ResponseEntity<ApiResponse<IncidentAnalysisResponse>> analyzeIncident(@PathVariable Long id) {
        IncidentAnalysisResponse response = incidentAiService.analyzeIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Analyse générée avec succès", response));
    }

    @PostMapping("/ai/chat")
    @Operation(summary = "Chat with the SLA Monitor AI assistant")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
