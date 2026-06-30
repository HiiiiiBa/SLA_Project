package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;

import java.util.List;

public interface AlertService {

    AlertResponse createAlert(AlertCreateRequest request);

    AlertResponse markAsRead(Long id);

    List<AlertResponse> findAll();

    AlertResponse findById(Long id);

    List<AlertResponse> findActiveAlerts();

    void deleteAlert(Long id);
}
