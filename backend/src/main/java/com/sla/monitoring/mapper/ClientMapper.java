package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ClientCreateRequest;
import com.sla.monitoring.dto.request.ClientUpdateRequest;
import com.sla.monitoring.dto.response.ClientResponse;
import com.sla.monitoring.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse toResponse(Client client);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slas", ignore = true)
    Client toEntity(ClientCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slas", ignore = true)
    void updateEntity(ClientUpdateRequest request, @MappingTarget Client client);
}
