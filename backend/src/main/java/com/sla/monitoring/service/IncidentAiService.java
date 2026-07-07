package com.sla.monitoring.service;

import com.sla.monitoring.dto.response.IncidentAnalysisResponse;

public interface IncidentAiService {

    IncidentAnalysisResponse analyzeIncident(Long incidentId);
}
