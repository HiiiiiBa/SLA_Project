package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ClientCreateRequest;
import com.sla.monitoring.dto.request.ClientUpdateRequest;
import com.sla.monitoring.dto.response.ClientResponse;
import com.sla.monitoring.entity.Client;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-30T09:44:30+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class ClientMapperImpl implements ClientMapper {

    @Override
    public ClientResponse toResponse(Client client) {
        if ( client == null ) {
            return null;
        }

        ClientResponse.ClientResponseBuilder clientResponse = ClientResponse.builder();

        clientResponse.id( client.getId() );
        clientResponse.name( client.getName() );
        clientResponse.email( client.getEmail() );
        clientResponse.projectName( client.getProjectName() );
        clientResponse.createdAt( client.getCreatedAt() );
        clientResponse.updatedAt( client.getUpdatedAt() );

        return clientResponse.build();
    }

    @Override
    public Client toEntity(ClientCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Client.ClientBuilder client = Client.builder();

        client.name( request.getName() );
        client.email( request.getEmail() );
        client.projectName( request.getProjectName() );

        return client.build();
    }

    @Override
    public void updateEntity(ClientUpdateRequest request, Client client) {
        if ( request == null ) {
            return;
        }

        client.setName( request.getName() );
        client.setEmail( request.getEmail() );
        client.setProjectName( request.getProjectName() );
    }
}
