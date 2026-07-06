package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.MonitoringMetricCreateRequest;
import com.sla.monitoring.dto.response.MonitoringMetricResponse;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-06T09:14:10+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class MonitoringMetricMapperImpl implements MonitoringMetricMapper {

    @Override
    public MonitoringMetricResponse toResponse(MonitoringMetric metric) {
        if ( metric == null ) {
            return null;
        }

        MonitoringMetricResponse.MonitoringMetricResponseBuilder monitoringMetricResponse = MonitoringMetricResponse.builder();

        monitoringMetricResponse.serviceId( metricServiceId( metric ) );
        monitoringMetricResponse.slaId( metricSlaId( metric ) );
        monitoringMetricResponse.id( metric.getId() );
        monitoringMetricResponse.timestamp( metric.getTimestamp() );
        monitoringMetricResponse.responseTime( metric.getResponseTime() );
        monitoringMetricResponse.status( metric.getStatus() );
        monitoringMetricResponse.errorRate( metric.getErrorRate() );
        monitoringMetricResponse.createdAt( metric.getCreatedAt() );
        monitoringMetricResponse.updatedAt( metric.getUpdatedAt() );

        return monitoringMetricResponse.build();
    }

    @Override
    public MonitoringMetric toEntity(MonitoringMetricCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        MonitoringMetric.MonitoringMetricBuilder monitoringMetric = MonitoringMetric.builder();

        monitoringMetric.timestamp( request.getTimestamp() );
        monitoringMetric.responseTime( request.getResponseTime() );
        monitoringMetric.status( request.getStatus() );
        monitoringMetric.errorRate( request.getErrorRate() );

        return monitoringMetric.build();
    }

    private Long metricServiceId(MonitoringMetric monitoringMetric) {
        Service service = monitoringMetric.getService();
        if ( service == null ) {
            return null;
        }
        return service.getId();
    }

    private Long metricSlaId(MonitoringMetric monitoringMetric) {
        Sla sla = monitoringMetric.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getId();
    }
}
