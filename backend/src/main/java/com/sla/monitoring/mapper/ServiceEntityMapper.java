package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ServiceEntityCreateRequest;
import com.sla.monitoring.dto.request.ServiceEntityUpdateRequest;
import com.sla.monitoring.dto.response.ServiceEntityResponse;
import com.sla.monitoring.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceEntityMapper {

    @Mapping(target = "slaId", source = "sla.id")
    ServiceEntityResponse toResponse(Service service);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sla", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    Service toEntity(ServiceEntityCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sla", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    void updateEntity(ServiceEntityUpdateRequest request, @MappingTarget Service service);
}
