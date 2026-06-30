package com.sla.monitoring.mapper;

import com.sla.monitoring.dto.request.ReportCreateRequest;
import com.sla.monitoring.dto.response.ReportResponse;
import com.sla.monitoring.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "slaId", source = "sla.id")
    ReportResponse toResponse(Report report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "generatedAt", ignore = true)
    @Mapping(target = "sla", ignore = true)
    Report toEntity(ReportCreateRequest request);
}
