package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.MaintenanceWindowCreateRequest;
import com.sla.monitoring.dto.response.MaintenanceWindowResponse;
import com.sla.monitoring.entity.MaintenanceWindow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaintenanceWindowMapper {

    @Mapping(target = "slaId", source = "sla.id")
    @Mapping(target = "slaName", source = "sla.name")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    MaintenanceWindowResponse toResponse(MaintenanceWindow window);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sla", ignore = true)
    @Mapping(target = "service", ignore = true)
    MaintenanceWindow toEntity(MaintenanceWindowCreateRequest request);
}
