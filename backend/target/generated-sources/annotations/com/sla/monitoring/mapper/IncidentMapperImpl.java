package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Sla;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T12:04:44+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class IncidentMapperImpl implements IncidentMapper {

    @Override
    public IncidentResponse toResponse(Incident incident) {
        if ( incident == null ) {
            return null;
        }

        IncidentResponse.IncidentResponseBuilder incidentResponse = IncidentResponse.builder();

        incidentResponse.slaId( incidentSlaId( incident ) );
        incidentResponse.id( incident.getId() );
        incidentResponse.startTime( incident.getStartTime() );
        incidentResponse.endTime( incident.getEndTime() );
        incidentResponse.severity( incident.getSeverity() );
        incidentResponse.description( incident.getDescription() );
        incidentResponse.createdAt( incident.getCreatedAt() );
        incidentResponse.updatedAt( incident.getUpdatedAt() );

        return incidentResponse.build();
    }

    @Override
    public Incident toEntity(IncidentCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Incident.IncidentBuilder incident = Incident.builder();

        incident.startTime( request.getStartTime() );
        incident.severity( request.getSeverity() );
        incident.description( request.getDescription() );

        return incident.build();
    }

    @Override
    public void updateEntity(IncidentUpdateRequest request, Incident incident) {
        if ( request == null ) {
            return;
        }

        incident.setStartTime( request.getStartTime() );
        incident.setEndTime( request.getEndTime() );
        incident.setSeverity( request.getSeverity() );
        incident.setDescription( request.getDescription() );
    }

    private Long incidentSlaId(Incident incident) {
        Sla sla = incident.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getId();
    }
}
