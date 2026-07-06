package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.IncidentMapper;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.IncidentService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final SlaRepository slaRepository;
    private final ProjectRepository projectRepository;
    private final IncidentMapper incidentMapper;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        Sla sla = findSlaById(request.getSlaId());
        employeeScopeService.assertSlaAccess(sla.getId());
        managerScopeService.assertSlaAccess(sla.getId());
        if (request.getProjectId() != null) {
            employeeScopeService.assertProjectAccess(request.getProjectId());
            managerScopeService.assertProjectAccess(request.getProjectId());
        }

        Incident incident = incidentMapper.toEntity(request);
        incident.setSla(sla);
        incident.setProject(resolveProject(request.getProjectId()));

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        Incident incident = findIncidentEntityById(id);
        validateIncidentDates(request.getStartTime(), request.getEndTime());

        incidentMapper.updateEntity(request, incident);
        incident.setProject(resolveProject(request.getProjectId()));

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse closeIncident(Long id) {
        Incident incident = findIncidentEntityById(id);

        if (incident.getEndTime() != null) {
            throw new BusinessException("Incident is already closed");
        }

        incident.setEndTime(LocalDateTime.now());
        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = findIncidentEntityById(id);
        incidentRepository.delete(incident);
    }

    @Override
    public List<IncidentResponse> findAll() {
        return filterVisible(incidentRepository.findAllWithDetails()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public IncidentResponse findById(Long id) {
        Incident incident = findIncidentEntityById(id);
        employeeScopeService.assertIncidentAccess(incident);
        managerScopeService.assertIncidentAccess(incident);
        clientScopeService.assertIncidentAccess(incident);
        return incidentMapper.toResponse(incident);
    }

    @Override
    public List<IncidentResponse> findOpenIncidents() {
        return filterVisible(incidentRepository.findByEndTimeIsNull()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySeverity(IncidentSeverity severity) {
        return filterVisible(incidentRepository.findBySeverity(severity)).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySlaId(Long slaId) {
        employeeScopeService.assertSlaAccess(slaId);
        managerScopeService.assertSlaAccess(slaId);
        clientScopeService.assertSlaAccess(slaId);
        if (!slaRepository.existsById(slaId)) {
            throw new ResourceNotFoundException("SLA", "id", slaId);
        }
        return filterVisible(incidentRepository.findBySlaId(slaId)).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findByProjectId(Long projectId) {
        employeeScopeService.assertProjectAccess(projectId);
        managerScopeService.assertProjectAccess(projectId);
        clientScopeService.assertProjectAccess(projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return incidentRepository.findByProjectId(projectId).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    private List<Incident> filterVisible(List<Incident> incidents) {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return incidents.stream()
                    .filter(employeeScopeService::isIncidentVisible)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            return incidents.stream()
                    .filter(managerScopeService::isIncidentVisible)
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            return incidents.stream()
                    .filter(clientScopeService::isIncidentVisible)
                    .toList();
        }
        return incidents;
    }

    private void validateIncidentDates(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException("End time must be after start time");
        }
    }

    private Incident findIncidentEntityById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }

    private Project resolveProject(Long projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }
}
