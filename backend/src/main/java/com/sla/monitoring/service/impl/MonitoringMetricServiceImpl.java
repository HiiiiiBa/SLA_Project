package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.alert.AutomaticAlertService;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.MonitoringMetricMapper;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.MonitoringMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringMetricServiceImpl implements MonitoringMetricService {

    private final MonitoringMetricRepository monitoringMetricRepository;
    private final ServiceRepository serviceRepository;
    private final SlaRepository slaRepository;
    private final MonitoringMetricMapper monitoringMetricMapper;
    private final AutomaticAlertService automaticAlertService;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

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

        MonitoringMetric saved = monitoringMetricRepository.save(metric);
        triggerAutomaticAlerts(saved, service, sla);

        return monitoringMetricMapper.toResponse(saved);
    }

    private void triggerAutomaticAlerts(MonitoringMetric metric, Service service, Sla sla) {
        if (metric.getStatus() == MetricStatus.DOWN) {
            automaticAlertService.createServiceDownAlert(service, sla);
        }
        if (metric.getErrorRate() > sla.getErrorRateLimit()) {
            automaticAlertService.createHighErrorRateAlert(service, sla, metric.getErrorRate());
        }
    }

    @Override
    public List<MonitoringMetricResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> slaIds = employeeScopeService.getScopedSlaIds();
            return slaIds.isEmpty()
                    ? List.of()
                    : filterAndMap(monitoringMetricRepository.findBySlaIdIn(slaIds));
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> slaIds = managerScopeService.getScopedSlaIds();
            return slaIds.isEmpty()
                    ? List.of()
                    : filterAndMap(monitoringMetricRepository.findBySlaIdIn(slaIds));
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> slaIds = clientScopeService.getScopedSlaIds();
            return slaIds.isEmpty()
                    ? List.of()
                    : filterAndMap(monitoringMetricRepository.findBySlaIdIn(slaIds));
        }
        return monitoringMetricRepository.findAll().stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    private List<MonitoringMetricResponse> filterAndMap(List<MonitoringMetric> metrics) {
        return metrics.stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    public MonitoringMetricResponse findById(Long id) {
        MonitoringMetric metric = monitoringMetricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonitoringMetric", "id", id));
        employeeScopeService.assertSlaAccess(metric.getSla().getId());
        managerScopeService.assertSlaAccess(metric.getSla().getId());
        clientScopeService.assertSlaAccess(metric.getSla().getId());
        return monitoringMetricMapper.toResponse(metric);
    }

    @Override
    public List<MonitoringMetricResponse> findByService(Long serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            throw new ResourceNotFoundException("Service", "id", serviceId);
        }
        Service service = findServiceById(serviceId);
        employeeScopeService.assertSlaAccess(service.getSla().getId());
        managerScopeService.assertSlaAccess(service.getSla().getId());
        clientScopeService.assertSlaAccess(service.getSla().getId());
        return monitoringMetricRepository.findByServiceId(serviceId).stream()
                .map(monitoringMetricMapper::toResponse)
                .toList();
    }

    @Override
    public List<MonitoringMetricResponse> findBySla(Long slaId) {
        employeeScopeService.assertSlaAccess(slaId);
        managerScopeService.assertSlaAccess(slaId);
        clientScopeService.assertSlaAccess(slaId);
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
        List<MonitoringMetric> metrics = monitoringMetricRepository.findByTimestampBetween(start, end);
        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> slaIds = employeeScopeService.getScopedSlaIds();
            metrics = metrics.stream()
                    .filter(metric -> slaIds.contains(metric.getSla().getId()))
                    .toList();
        } else if (managerScopeService.isCurrentUserManager()) {
            Set<Long> slaIds = managerScopeService.getScopedSlaIds();
            metrics = metrics.stream()
                    .filter(metric -> slaIds.contains(metric.getSla().getId()))
                    .toList();
        } else if (clientScopeService.isCurrentUserClient()) {
            Set<Long> slaIds = clientScopeService.getScopedSlaIds();
            metrics = metrics.stream()
                    .filter(metric -> slaIds.contains(metric.getSla().getId()))
                    .toList();
        }
        return metrics.stream()
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
