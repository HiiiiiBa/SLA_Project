package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.IncidentMapper;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.IncidentService;
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
    private final IncidentMapper incidentMapper;

    @Override
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        Sla sla = findSlaById(request.getSlaId());

        Incident incident = incidentMapper.toEntity(request);
        incident.setSla(sla);

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        Incident incident = findIncidentEntityById(id);
        validateIncidentDates(request.getStartTime(), request.getEndTime());

        incidentMapper.updateEntity(request, incident);

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
        return incidentRepository.findAll().stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public IncidentResponse findById(Long id) {
        return incidentMapper.toResponse(findIncidentEntityById(id));
    }

    @Override
    public List<IncidentResponse> findOpenIncidents() {
        return incidentRepository.findByEndTimeIsNull().stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySeverity(IncidentSeverity severity) {
        return incidentRepository.findBySeverity(severity).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySlaId(Long slaId) {
        if (!slaRepository.existsById(slaId)) {
            throw new ResourceNotFoundException("SLA", "id", slaId);
        }
        return incidentRepository.findBySlaId(slaId).stream()
                .map(incidentMapper::toResponse)
                .toList();
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
}
