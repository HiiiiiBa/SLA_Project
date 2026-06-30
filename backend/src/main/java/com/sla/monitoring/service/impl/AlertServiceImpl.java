package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.AlertMapper;
import com.sla.monitoring.notification.AlertNotificationService;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final SlaRepository slaRepository;
    private final AlertMapper alertMapper;
    private final AlertNotificationService alertNotificationService;

    @Override
    @Transactional
    public AlertResponse createAlert(AlertCreateRequest request) {
        Sla sla = findSlaById(request.getSlaId());

        Alert alert = alertMapper.toEntity(request);
        alert.setSla(sla);
        alert.setStatus(AlertStatus.NEW);

        Alert savedAlert = alertRepository.save(alert);
        alertNotificationService.dispatch(savedAlert.getId());
        return alertMapper.toResponse(savedAlert);
    }

    @Override
    @Transactional
    public AlertResponse markAsRead(Long id) {
        Alert alert = findAlertEntityById(id);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BusinessException("Cannot mark a resolved alert as read");
        }

        alert.setStatus(AlertStatus.READ);
        return alertMapper.toResponse(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public AlertResponse resolveAlert(Long id) {
        Alert alert = findAlertEntityById(id);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BusinessException("Alert is already resolved");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        return alertMapper.toResponse(alertRepository.save(alert));
    }

    @Override
    public List<AlertResponse> findAll() {
        return alertRepository.findAllWithDetails().stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @Override
    public List<AlertResponse> findFiltered(Long slaId, Long serviceId, AlertType type, AlertStatus status) {
        return alertRepository.findFiltered(slaId, serviceId, type, status).stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @Override
    public AlertResponse findById(Long id) {
        return alertMapper.toResponse(findAlertEntityById(id));
    }

    @Override
    public List<AlertResponse> findActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.NEW).stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAlert(Long id) {
        Alert alert = findAlertEntityById(id);
        alertRepository.delete(alert);
    }

    private Alert findAlertEntityById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
