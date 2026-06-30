package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.MonitoringMetricMapper;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.MonitoringMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringMetricServiceImpl implements MonitoringMetricService {

    private final MonitoringMetricRepository monitoringMetricRepository;
    private final ServiceRepository serviceRepository;
    private final SlaRepository slaRepository;
    private final MonitoringMetricMapper monitoringMetricMapper;

    @Override
    @Transactional
    public MonitoringMetricResponse addMetric(MonitoringMetricCreateRequest request) {
        Service service = findServiceById(request.getServiceId());
        Sla sla = findSlaById(request.getSlaId());

        if (!service.getSla().getId().equals(sla.getId())) {
            throw new BusinessException("Service does not belong to the specified SLA");
        }

        MonitoringMetric metric = monitoringMetricMapper.toEntity(request);
        metric.setService(service);
        metric.setSla(sla);

        return monitoringMetricMapper.toResponse(monitoringMetricRepository.save(metric));
    }

    @Override
    public List<MonitoringMetricResponse> findAll() {
        return monitoringMetricRepository.findAll().stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    public MonitoringMetricResponse findById(Long id) {
        MonitoringMetric metric = monitoringMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonitoringMetric", "id", id));
        return monitoringMetricMapper.toResponse(metric);
    }

    @Override
    public List<MonitoringMetricResponse> findByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Service", "id", serviceId);
        }
        return monitoringMetricRepository.findByServiceId(serviceId).stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    public List<MonitoringMetricResponse> findBySla(Long slaId) {
        if (!slaRepository.existsById(slaId)) {
            throw new ResourceNotFoundException("SLA", "id", slaId);
        }
        return monitoringMetricRepository.findBySlaId(slaId).stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    public List<MonitoringMetricResponse> findByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BusinessException("Start date must be before end date");
        }
        return monitoringMetricRepository.findByTimestampBetween(start, end).stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMetric(Long id) {
        MonitoringMetric metric = monitoringMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonitoringMetric", "id", id));
        monitoringMetricRepository.delete(metric);
    }

    private Service findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
