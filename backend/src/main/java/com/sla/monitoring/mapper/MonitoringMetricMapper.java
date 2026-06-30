package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.entity.MonitoringMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MonitoringMetricMapper {

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "slaId", source = "sla.id")
    MonitoringMetricResponse toResponse(MonitoringMetric metric);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "sla", ignore = true)
    MonitoringMetric toEntity(MonitoringMetricCreateRequest request);
}
