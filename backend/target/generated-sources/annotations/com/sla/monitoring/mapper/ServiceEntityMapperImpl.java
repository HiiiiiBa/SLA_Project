package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ServiceEntityCreateRequest;
import com.sla.monitoring.dto.request.ServiceEntityUpdateRequest;
import com.sla.monitoring.dto.response.ServiceEntityResponse;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-06T09:53:29+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class ServiceEntityMapperImpl implements ServiceEntityMapper {

    @Override
    public ServiceEntityResponse toResponse(Service service) {
        if ( service == null ) {
            return null;
        }

        ServiceEntityResponse.ServiceEntityResponseBuilder serviceEntityResponse = ServiceEntityResponse.builder();

        serviceEntityResponse.slaId( serviceSlaId( service ) );
        serviceEntityResponse.slaName( serviceSlaName( service ) );
        serviceEntityResponse.id( service.getId() );
        serviceEntityResponse.name( service.getName() );
        serviceEntityResponse.status( service.getStatus() );
        serviceEntityResponse.createdAt( service.getCreatedAt() );
        serviceEntityResponse.updatedAt( service.getUpdatedAt() );

        return serviceEntityResponse.build();
    }

    @Override
    public Service toEntity(ServiceEntityCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Service.ServiceBuilder service = Service.builder();

        service.name( request.getName() );
        service.status( request.getStatus() );

        return service.build();
    }

    @Override
    public void updateEntity(ServiceEntityUpdateRequest request, Service service) {
        if ( request == null ) {
            return;
        }

        service.setName( request.getName() );
        service.setStatus( request.getStatus() );
    }

    private Long serviceSlaId(Service service) {
        Sla sla = service.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getId();
    }

    private String serviceSlaName(Service service) {
        Sla sla = service.getSla();
        if ( sla == null ) {
            return null;
        }
        return sla.getName();
    }
}
