package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "slaId", source = "sla.id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "sla", ignore = true)
    @Mapping(target = "project", ignore = true)
    Incident toEntity(IncidentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sla", ignore = true)
    @Mapping(target = "project", ignore = true)
    void updateEntity(IncidentUpdateRequest request, @MappingTarget Incident incident);
}
