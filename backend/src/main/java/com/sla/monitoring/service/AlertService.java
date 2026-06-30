package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;

import java.util.List;

public interface AlertService {

    AlertResponse createAlert(AlertCreateRequest request);

    AlertResponse markAsRead(Long id);

    AlertResponse resolveAlert(Long id);

    List<AlertResponse> findAll();

    List<AlertResponse> findFiltered(Long slaId, Long serviceId, AlertType type, AlertStatus status);

    AlertResponse findById(Long id);

    List<AlertResponse> findActiveAlerts();

    void deleteAlert(Long id);
}
