package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.MaintenanceWindowCreateRequest;
import com.sla.monitoring.dto.request.MaintenanceWindowUpdateRequest;
import com.sla.monitoring.dto.response.MaintenanceWindowResponse;
import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;

import java.util.List;

public interface MaintenanceWindowService {

    List<MaintenanceWindowResponse> findAll();

    List<MaintenanceWindowResponse> findFiltered(Long slaId, MaintenanceWindowStatus status);

    MaintenanceWindowResponse findById(Long id);

    MaintenanceWindowResponse create(MaintenanceWindowCreateRequest request);

    MaintenanceWindowResponse update(Long id, MaintenanceWindowUpdateRequest request);

    MaintenanceWindowResponse cancel(Long id);
}
