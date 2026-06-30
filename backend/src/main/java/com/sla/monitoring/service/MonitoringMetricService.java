package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface MonitoringMetricService {

    MonitoringMetricResponse addMetric(MonitoringMetricCreateRequest request);

    List<MonitoringMetricResponse> findAll();

    MonitoringMetricResponse findById(Long id);

    List<MonitoringMetricResponse> findByService(Long serviceId);

    List<MonitoringMetricResponse> findBySla(Long slaId);

    List<MonitoringMetricResponse> findByDateRange(LocalDateTime start, LocalDateTime end);

    void deleteMetric(Long id);
}
