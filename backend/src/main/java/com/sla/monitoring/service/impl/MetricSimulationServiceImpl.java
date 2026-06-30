package com.sla.monitoring.service.impl;

import com.sla.monitoring.config.MetricSimulationProperties;
import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MetricSimulationResponse;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.MetricSimulationService;
import com.sla.monitoring.service.MonitoringMetricService;
import com.sla.monitoring.simulation.MetricSimulator;
import com.sla.monitoring.simulation.SimulationScenario;
import com.sla.monitoring.simulation.model.SimulatedMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists simulated monitoring metrics for active services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MetricSimulationServiceImpl implements MetricSimulationService {

    private final SlaRepository slaRepository;
    private final ServiceRepository serviceRepository;
    private final MonitoringMetricService monitoringMetricService;
    private final MetricSimulator metricSimulator;
    private final MetricSimulationProperties metricSimulationProperties;

    @Override
    public MetricSimulationResponse simulateAll(SimulationScenario scenario) {
        List<Sla> activeSlas = slaRepository.findByStatus(SlaStatus.ACTIVE);
        List<MonitoringMetricResponse> generatedMetrics = new ArrayList<>();
        int servicesProcessed = 0;

        for (Sla sla : activeSlas) {
            List<com.sla.monitoring.entity.Service> services = serviceRepository.findBySlaId(sla.getId());
            for (com.sla.monitoring.entity.Service service : services) {
                generatedMetrics.add(simulateAndPersist(service, sla, scenario));
                servicesProcessed++;
            }
        }

        log.info("Metric simulation completed: scenario={}, services={}, metrics={}",
                scenario, servicesProcessed, generatedMetrics.size());

        return buildResponse(scenario, servicesProcessed, generatedMetrics);
    }

    @Override
    public MetricSimulationResponse simulateForSla(Long slaId, SimulationScenario scenario) {
        Sla sla = findActiveSlaById(slaId);
        List<com.sla.monitoring.entity.Service> services = serviceRepository.findBySlaId(sla.getId());

        if (services.isEmpty()) {
            throw new ResourceNotFoundException("Service", "slaId", slaId);
        }

        List<MonitoringMetricResponse> generatedMetrics = services.stream()
                .map(service -> simulateAndPersist(service, sla, scenario))
                .toList();

        return buildResponse(scenario, services.size(), generatedMetrics);
    }

    @Override
    public MetricSimulationResponse simulateForService(Long serviceId, SimulationScenario scenario) {
        com.sla.monitoring.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

        Sla sla = service.getSla();
        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            throw new ResourceNotFoundException("Active SLA", "id", sla.getId());
        }

        MonitoringMetricResponse metric = simulateAndPersist(service, sla, scenario);
        return buildResponse(scenario, 1, List.of(metric));
    }

    private MonitoringMetricResponse simulateAndPersist(com.sla.monitoring.entity.Service service,
                                                        Sla sla,
                                                        SimulationScenario scenario) {
        SimulatedMetric simulated = metricSimulator.simulate(service, sla, scenario);
        syncServiceStatus(service, simulated.getStatus());

        MonitoringMetricCreateRequest request = MonitoringMetricCreateRequest.builder()
                .timestamp(simulated.getTimestamp())
                .responseTime(simulated.getResponseTime())
                .status(simulated.getStatus())
                .errorRate(simulated.getErrorRate())
                .serviceId(simulated.getServiceId())
                .slaId(simulated.getSlaId())
                .build();

        return monitoringMetricService.addMetric(request);
    }

    private void syncServiceStatus(com.sla.monitoring.entity.Service service, MetricStatus metricStatus) {
        if (!metricSimulationProperties.isSyncServiceStatus()) {
            return;
        }

        ServiceStatus serviceStatus = metricStatus == MetricStatus.UP
                ? ServiceStatus.UP
                : ServiceStatus.DOWN;

        if (service.getStatus() != serviceStatus) {
            service.setStatus(serviceStatus);
            serviceRepository.save(service);
        }
    }

    private Sla findActiveSlaById(Long slaId) {
        Sla sla = slaRepository.findById(slaId)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", slaId));

        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            throw new ResourceNotFoundException("Active SLA", "id", slaId);
        }
        return sla;
    }

    private MetricSimulationResponse buildResponse(SimulationScenario scenario,
                                                   int servicesProcessed,
                                                   List<MonitoringMetricResponse> metrics) {
        return MetricSimulationResponse.builder()
                .scenario(scenario)
                .servicesProcessed(servicesProcessed)
                .metricsGenerated(metrics.size())
                .metrics(metrics)
                .build();
    }
}
