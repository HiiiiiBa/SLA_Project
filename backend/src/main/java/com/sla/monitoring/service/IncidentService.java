package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.enums.IncidentSeverity;

import java.util.List;

public interface IncidentService {

    IncidentResponse createIncident(IncidentCreateRequest request);

    IncidentResponse updateIncident(Long id, IncidentUpdateRequest request);

    IncidentResponse closeIncident(Long id);

    void deleteIncident(Long id);

    List<IncidentResponse> findAll();

    IncidentResponse findById(Long id);

    List<IncidentResponse> findOpenIncidents();

    List<IncidentResponse> findBySeverity(IncidentSeverity severity);

    List<IncidentResponse> findBySlaId(Long slaId);

    List<IncidentResponse> findByProjectId(Long projectId);
}
