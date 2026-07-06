package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.AlertCreateRequest;
import com.sla.monitoring.dto.response.AlertResponse;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.mapper.AlertMapper;
import com.sla.monitoring.notification.AlertNotificationService;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.AlertService;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final SlaRepository slaRepository;
    private final AlertMapper alertMapper;
    private final AlertNotificationService alertNotificationService;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

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
        assertCanMutateAlert(alert);

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
        assertCanMutateAlert(alert);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BusinessException("Alert is already resolved");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        return alertMapper.toResponse(alertRepository.save(alert));
    }

    @Override
    public List<AlertResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> slaIds = employeeScopeService.getScopedSlaIds();
            if (slaIds.isEmpty()) {
                return List.of();
            }
            return alertRepository.findBySlaIdIn(slaIds).stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> slaIds = managerScopeService.getScopedSlaIds();
            if (slaIds.isEmpty()) {
                return List.of();
            }
            return alertRepository.findBySlaIdIn(slaIds).stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> slaIds = clientScopeService.getScopedSlaIds();
            if (slaIds.isEmpty()) {
                return List.of();
            }
            return alertRepository.findBySlaIdIn(slaIds).stream()
                    .map(alertMapper::toResponse)
                    .toList();
        }
        return alertRepository.findAllWithDetails().stream()
                .map(alertMapper::toResponse)
                .toList();
    }

    @Override
    public List<AlertResponse> findFiltered(Long slaId, Long serviceId, AlertType type, AlertStatus status) {
        if (slaId != null) {
            employeeScopeService.assertSlaAccess(slaId);
            managerScopeService.assertSlaAccess(slaId);
            clientScopeService.assertSlaAccess(slaId);
        }
        List<AlertResponse> alerts = alertRepository.findFiltered(slaId, serviceId, type, status).stream()
                .map(alertMapper::toResponse)
                .toList();
        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> scopedSlaIds = employeeScopeService.getScopedSlaIds();
            return alerts.stream()
                    .filter(alert -> scopedSlaIds.contains(alert.getSlaId()))
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> scopedSlaIds = managerScopeService.getScopedSlaIds();
            return alerts.stream()
                    .filter(alert -> scopedSlaIds.contains(alert.getSlaId()))
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> scopedSlaIds = clientScopeService.getScopedSlaIds();
            return alerts.stream()
                    .filter(alert -> scopedSlaIds.contains(alert.getSlaId()))
                    .toList();
        }
        return alerts;
    }

    @Override
    public AlertResponse findById(Long id) {
        Alert alert = findAlertEntityById(id);
        employeeScopeService.assertSlaAccess(alert.getSla().getId());
        managerScopeService.assertSlaAccess(alert.getSla().getId());
        clientScopeService.assertSlaAccess(alert.getSla().getId());
        return alertMapper.toResponse(alert);
    }

    @Override
    public List<AlertResponse> findActiveAlerts() {
        return findAll().stream()
                .filter(alert -> alert.getStatus() == AlertStatus.NEW)
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

    private void assertCanMutateAlert(Alert alert) {
        managerScopeService.assertSlaAccess(alert.getSla().getId());
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
