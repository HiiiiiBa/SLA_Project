package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.MaintenanceWindowCreateRequest;
import com.sla.monitoring.dto.request.MaintenanceWindowUpdateRequest;
import com.sla.monitoring.dto.response.MaintenanceWindowResponse;
import com.sla.monitoring.entity.MaintenanceWindow;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.MaintenanceWindowStatus;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.MaintenanceWindowMapper;
import com.sla.monitoring.repository.MaintenanceWindowRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.MaintenanceWindowService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceWindowServiceImpl implements MaintenanceWindowService {

    private final MaintenanceWindowRepository maintenanceWindowRepository;
    private final SlaRepository slaRepository;
    private final ServiceRepository serviceRepository;
    private final MaintenanceWindowMapper maintenanceWindowMapper;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    public List<MaintenanceWindowResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return mapScoped(employeeScopeService.getScopedSlaIds());
        }
        if (managerScopeService.isCurrentUserManager()) {
            return mapScoped(managerScopeService.getScopedSlaIds());
        }
        if (clientScopeService.isCurrentUserClient()) {
            return mapScoped(clientScopeService.getScopedSlaIds());
        }
        return maintenanceWindowRepository.findAllWithDetails().stream()
                .map(this::refreshAndMap)
                .toList();
    }

    @Override
    public List<MaintenanceWindowResponse> findFiltered(Long slaId, MaintenanceWindowStatus status) {
        if (slaId != null) {
            employeeScopeService.assertSlaAccess(slaId);
            managerScopeService.assertSlaAccess(slaId);
            clientScopeService.assertSlaAccess(slaId);
        }

        // Status is derived from dates; filter in memory after mapping.
        List<MaintenanceWindowResponse> windows = maintenanceWindowRepository.findFiltered(slaId, null).stream()
                .map(this::refreshAndMap)
                .toList();

        if (status != null) {
            windows = windows.stream().filter(w -> w.getStatus() == status).toList();
        }

        if (employeeScopeService.isCurrentUserEmployee()) {
            Set<Long> scoped = employeeScopeService.getScopedSlaIds();
            return windows.stream().filter(w -> scoped.contains(w.getSlaId())).toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> scoped = managerScopeService.getScopedSlaIds();
            return windows.stream().filter(w -> scoped.contains(w.getSlaId())).toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> scoped = clientScopeService.getScopedSlaIds();
            return windows.stream().filter(w -> scoped.contains(w.getSlaId())).toList();
        }
        return windows;
    }

    @Override
    public MaintenanceWindowResponse findById(Long id) {
        MaintenanceWindow window = findEntityById(id);
        employeeScopeService.assertSlaAccess(window.getSla().getId());
        managerScopeService.assertSlaAccess(window.getSla().getId());
        clientScopeService.assertSlaAccess(window.getSla().getId());
        return refreshAndMap(window);
    }

    @Override
    @Transactional
    public MaintenanceWindowResponse create(MaintenanceWindowCreateRequest request) {
        validateTimes(request.getStartTime(), request.getEndTime());
        Sla sla = findSlaById(request.getSlaId());
        managerScopeService.assertSlaAccess(sla.getId());

        MaintenanceWindow window = maintenanceWindowMapper.toEntity(request);
        window.setSla(sla);
        window.setService(resolveService(request.getServiceId(), sla.getId()));
        window.setStatus(deriveStatus(request.getStartTime(), request.getEndTime(), MaintenanceWindowStatus.SCHEDULED));

        return refreshAndMap(maintenanceWindowRepository.save(window));
    }

    @Override
    @Transactional
    public MaintenanceWindowResponse update(Long id, MaintenanceWindowUpdateRequest request) {
        MaintenanceWindow window = findEntityById(id);
        managerScopeService.assertSlaAccess(window.getSla().getId());

        if (window.getStatus() == MaintenanceWindowStatus.CANCELLED
                || window.getStatus() == MaintenanceWindowStatus.COMPLETED) {
            throw new BusinessException("Cannot update a cancelled or completed maintenance window");
        }

        validateTimes(request.getStartTime(), request.getEndTime());
        window.setTitle(request.getTitle());
        window.setReason(request.getReason());
        window.setStartTime(request.getStartTime());
        window.setEndTime(request.getEndTime());
        window.setService(resolveService(request.getServiceId(), window.getSla().getId()));
        window.setStatus(deriveStatus(request.getStartTime(), request.getEndTime(), window.getStatus()));

        return refreshAndMap(maintenanceWindowRepository.save(window));
    }

    @Override
    @Transactional
    public MaintenanceWindowResponse cancel(Long id) {
        MaintenanceWindow window = findEntityById(id);
        managerScopeService.assertSlaAccess(window.getSla().getId());

        if (window.getStatus() == MaintenanceWindowStatus.CANCELLED) {
            throw new BusinessException("Maintenance window is already cancelled");
        }
        if (window.getStatus() == MaintenanceWindowStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed maintenance window");
        }

        window.setStatus(MaintenanceWindowStatus.CANCELLED);
        return maintenanceWindowMapper.toResponse(maintenanceWindowRepository.save(window));
    }

    private List<MaintenanceWindowResponse> mapScoped(Set<Long> slaIds) {
        if (slaIds.isEmpty()) {
            return List.of();
        }
        return maintenanceWindowRepository.findBySlaIdInWithDetails(slaIds).stream()
                .map(this::refreshAndMap)
                .toList();
    }

    private MaintenanceWindowResponse refreshAndMap(MaintenanceWindow window) {
        MaintenanceWindowResponse response = maintenanceWindowMapper.toResponse(window);
        if (window.getStatus() != MaintenanceWindowStatus.CANCELLED) {
            response.setStatus(deriveStatus(window.getStartTime(), window.getEndTime(), window.getStatus()));
        }
        return response;
    }

    private MaintenanceWindowStatus deriveStatus(LocalDateTime start,
                                                 LocalDateTime end,
                                                 MaintenanceWindowStatus current) {
        if (current == MaintenanceWindowStatus.CANCELLED) {
            return MaintenanceWindowStatus.CANCELLED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return MaintenanceWindowStatus.SCHEDULED;
        }
        if (now.isBefore(end)) {
            return MaintenanceWindowStatus.ACTIVE;
        }
        return MaintenanceWindowStatus.COMPLETED;
    }

    private void validateTimes(LocalDateTime start, LocalDateTime end) {
        if (end == null || start == null || !end.isAfter(start)) {
            throw new BusinessException("End time must be after start time");
        }
    }

    private com.sla.monitoring.entity.Service resolveService(Long serviceId, Long slaId) {
        if (serviceId == null) {
            return null;
        }
        com.sla.monitoring.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));
        if (!service.getSla().getId().equals(slaId)) {
            throw new BusinessException("Service does not belong to the selected SLA");
        }
        return service;
    }

    private MaintenanceWindow findEntityById(Long id) {
        return maintenanceWindowRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance window", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
