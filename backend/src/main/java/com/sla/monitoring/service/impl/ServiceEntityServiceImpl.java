package com.sla.monitoring.service.impl;

import com.sla.monitoring.alert.AutomaticAlertService;
import com.sla.monitoring.dto.request.ServiceEntityCreateRequest;
import com.sla.monitoring.dto.request.ServiceEntityUpdateRequest;
import com.sla.monitoring.dto.request.ServiceStatusChangeRequest;
import com.sla.monitoring.dto.response.ServiceEntityResponse;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.ServiceEntityMapper;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceEntityServiceImpl implements ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final SlaRepository slaRepository;
    private final ServiceEntityMapper serviceEntityMapper;
    private final AutomaticAlertService automaticAlertService;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    @Transactional
    public ServiceEntityResponse createService(ServiceEntityCreateRequest request) {
        validateStatus(request.getStatus());
        managerScopeService.assertSlaAccess(request.getSlaId());

        Sla sla = findSlaById(request.getSlaId());
        Service service = serviceEntityMapper.toEntity(request);
        service.setSla(sla);

        return serviceEntityMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceEntityResponse updateService(Long id, ServiceEntityUpdateRequest request) {
        validateStatus(request.getStatus());

        Service service = findServiceEntityById(id);
        managerScopeService.assertSlaAccess(service.getSla().getId());
        ServiceStatus previousStatus = service.getStatus();
        serviceEntityMapper.updateEntity(request, service);

        if (request.getSlaId() != null && !request.getSlaId().equals(service.getSla().getId())) {
            managerScopeService.assertSlaAccess(request.getSlaId());
            service.setSla(findSlaById(request.getSlaId()));
        }

        Service saved = serviceRepository.save(service);
        notifyIfServiceDown(saved, previousStatus);
        return serviceEntityMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        Service service = findServiceEntityById(id);
        managerScopeService.assertSlaAccess(service.getSla().getId());
        serviceRepository.delete(service);
    }

    @Override
    public java.util.List<ServiceEntityResponse> findAll(Long slaId) {
        java.util.List<Service> services;
        if (employeeScopeService.isCurrentUserEmployee()) {
            java.util.Set<Long> scopedSlaIds = employeeScopeService.getScopedSlaIds();
            if (scopedSlaIds.isEmpty()) {
                return java.util.List.of();
            }
            if (slaId != null) {
                employeeScopeService.assertSlaAccess(slaId);
                services = serviceRepository.findBySlaIdWithSla(slaId);
            } else {
                services = serviceRepository.findBySlaIdInWithSla(scopedSlaIds);
            }
        } else if (managerScopeService.isCurrentUserManager()) {
            java.util.Set<Long> scopedSlaIds = managerScopeService.getScopedSlaIds();
            if (scopedSlaIds.isEmpty()) {
                return java.util.List.of();
            }
            if (slaId != null) {
                managerScopeService.assertSlaAccess(slaId);
                services = serviceRepository.findBySlaIdWithSla(slaId);
            } else {
                services = serviceRepository.findBySlaIdInWithSla(scopedSlaIds);
            }
        } else if (clientScopeService.isCurrentUserClient()) {
            java.util.Set<Long> scopedSlaIds = clientScopeService.getScopedSlaIds();
            if (scopedSlaIds.isEmpty()) {
                return java.util.List.of();
            }
            if (slaId != null) {
                clientScopeService.assertSlaAccess(slaId);
                services = serviceRepository.findBySlaIdWithSla(slaId);
            } else {
                services = serviceRepository.findBySlaIdInWithSla(scopedSlaIds);
            }
        } else {
            services = slaId == null
                    ? serviceRepository.findAllWithSla()
                    : serviceRepository.findBySlaIdWithSla(slaId);
        }

        return services.stream()
                .map(serviceEntityMapper::toResponse)
                .toList();
    }

    @Override
    public java.util.List<ServiceEntityResponse> findBySlaId(Long slaId) {
        return findAll(slaId);
    }

    @Override
    public ServiceEntityResponse findById(Long id) {
        Service service = findServiceEntityById(id);
        employeeScopeService.assertSlaAccess(service.getSla().getId());
        managerScopeService.assertSlaAccess(service.getSla().getId());
        clientScopeService.assertSlaAccess(service.getSla().getId());
        return serviceEntityMapper.toResponse(service);
    }

    @Override
    @Transactional
    public ServiceEntityResponse changeStatus(Long id, ServiceStatusChangeRequest request) {
        validateStatus(request.getStatus());

        Service service = findServiceEntityById(id);
        managerScopeService.assertSlaAccess(service.getSla().getId());
        ServiceStatus previousStatus = service.getStatus();
        service.setStatus(request.getStatus());

        Service saved = serviceRepository.save(service);
        notifyIfServiceDown(saved, previousStatus);
        return serviceEntityMapper.toResponse(saved);
    }

    private void notifyIfServiceDown(Service service, ServiceStatus previousStatus) {
        if (service.getStatus() == ServiceStatus.DOWN && previousStatus != ServiceStatus.DOWN) {
            automaticAlertService.createServiceDownAlert(service, service.getSla());
        }
    }

    private void validateStatus(ServiceStatus status) {
        if (status != ServiceStatus.UP && status != ServiceStatus.DOWN) {
            throw new BusinessException("Service status must be UP or DOWN");
        }
    }

    private Service findServiceEntityById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }
}
