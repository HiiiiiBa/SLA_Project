package com.sla.monitoring.service;

import com.sla.monitoring.dto.response.SlaEvaluationResponse;

import java.util.List;

/**
 * Orchestrates SLA evaluation, status updates, alerts and report generation.
 */
public interface SlaEngineService {

    /**
     * Evaluates all non-archived SLAs.
     */
    List<SlaEvaluationResponse> evaluateAll();

    /**
     * Evaluates a single SLA by identifier.
     */
    SlaEvaluationResponse evaluateById(Long slaId);
}
