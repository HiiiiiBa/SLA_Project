package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.Alert;
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
public class AlertMapperImpl implements AlertMapper {

    @Override
    public AlertResponse toResponse(Alert alert) {
        if ( alert == null ) {
            return null;
        }

        AlertResponse.AlertResponseBuilder alertResponse = AlertResponse.builder();

        alertResponse.slaId( alertSlaId( alert ) );
        alertResponse.slaName( alertSlaName( alert ) );
        alertResponse.serviceId( alertServiceId( alert ) );
        alertResponse.serviceName( alertServiceName( alert ) );
        alertResponse.id( alert.getId() );
        alertResponse.type( alert.getType() );
        alertResponse.message( alert.getMessage() );
        alertResponse.status( alert.getStatus() );
        alertResponse.createdAt( alert.getCreatedAt() );
        alertResponse.updatedAt( alert.getUpdatedAt() );

        return alertResponse.build();
    }

    @Override
    public Alert toEntity(AlertCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Alert.AlertBuilder alert = Alert.builder();

        alert.type( request.getType() );
        alert.message( request.getMessage() );

        return alert.build();
    }

    private Long alertSlaId(Alert alert) {
        Sla sla = alert.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getId();
    }

    private String alertSlaName(Alert alert) {
        Sla sla = alert.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getName();
    }

    private Long alertServiceId(Alert alert) {
        Service service = alert.getService();
        if ( service == null ) {
            return null;
        }
        return service.getId();
    }

    private String alertServiceName(Alert alert) {
        Service service = alert.getService();
        if ( service == null ) {
            return null;
        }
        return service.getName();
    }
}
