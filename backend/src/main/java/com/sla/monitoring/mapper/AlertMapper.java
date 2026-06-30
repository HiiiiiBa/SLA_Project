package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.Alert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    @Mapping(target = "slaId", source = "sla.id")
    AlertResponse toResponse(Alert alert);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sla", ignore = true)
    Alert toEntity(AlertCreateRequest request);
}
