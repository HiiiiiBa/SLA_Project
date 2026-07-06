package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.SlaCreateRequest;
import com.sla.monitoring.dto.request.SlaUpdateRequest;
import com.sla.monitoring.dto.response.SlaResponse;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Sla;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-06T09:53:29+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class SlaMapperImpl implements SlaMapper {

    @Override
    public SlaResponse toResponse(Sla sla) {
        if ( sla == null ) {
            return null;
        }

        SlaResponse.SlaResponseBuilder slaResponse = SlaResponse.builder();

        slaResponse.clientId( slaClientId( sla ) );
        slaResponse.clientName( slaClientName( sla ) );
        slaResponse.id( sla.getId() );
        slaResponse.name( sla.getName() );
        slaResponse.status( sla.getStatus() );
        slaResponse.uptimeTarget( sla.getUptimeTarget() );
        slaResponse.responseTimeLimit( sla.getResponseTimeLimit() );
        slaResponse.errorRateLimit( sla.getErrorRateLimit() );
        slaResponse.createdAt( sla.getCreatedAt() );
        slaResponse.updatedAt( sla.getUpdatedAt() );

        return slaResponse.build();
    }

    @Override
    public Sla toEntity(SlaCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Sla.SlaBuilder sla = Sla.builder();

        sla.name( request.getName() );
        sla.status( request.getStatus() );
        sla.uptimeTarget( request.getUptimeTarget() );
        sla.responseTimeLimit( request.getResponseTimeLimit() );
        sla.errorRateLimit( request.getErrorRateLimit() );

        return sla.build();
    }

    @Override
    public void updateEntity(SlaUpdateRequest request, Sla sla) {
        if ( request == null ) {
            return;
        }

        sla.setName( request.getName() );
        sla.setUptimeTarget( request.getUptimeTarget() );
        sla.setResponseTimeLimit( request.getResponseTimeLimit() );
        sla.setErrorRateLimit( request.getErrorRateLimit() );
    }

    private Long slaClientId(Sla sla) {
        Client client = sla.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getId();
    }

    private String slaClientName(Sla sla) {
        Client client = sla.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getName();
    }
}
