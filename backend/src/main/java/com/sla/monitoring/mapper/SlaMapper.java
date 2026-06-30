package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.SlaCreateRequest;
import com.sla.monitoring.dto.request.SlaUpdateRequest;
import com.sla.monitoring.dto.response.SlaResponse;
import com.sla.monitoring.entity.Sla;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SlaMapper {

    @Mapping(target = "clientId", source = "client.id")
    SlaResponse toResponse(Sla sla);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "incidents", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    @Mapping(target = "reports", ignore = true)
    Sla toEntity(SlaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    @Mapping(target = "incidents", ignore = true)
    @Mapping(target = "alerts", ignore = true)
    @Mapping(target = "reports", ignore = true)
    void updateEntity(SlaUpdateRequest request, @MappingTarget Sla sla);
}
