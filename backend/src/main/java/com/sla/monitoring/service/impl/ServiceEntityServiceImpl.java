package com.sla.monitoring.service.impl;

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

    @Override
    @Transactional
    public ServiceEntityResponse createService(ServiceEntityCreateRequest request) {
        validateStatus(request.getStatus());

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
        serviceEntityMapper.updateEntity(request, service);

        return serviceEntityMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        Service service = findServiceEntityById(id);
        serviceRepository.delete(service);
    }

    @Override
    public java.util.List<ServiceEntityResponse> findAll() {
        return serviceRepository.findAll().stream()
                .map(serviceEntityMapper::toResponse)
                .toList();
    }

    @Override
    public ServiceEntityResponse findById(Long id) {
        return serviceEntityMapper.toResponse(findServiceEntityById(id));
    }

    @Override
    @Transactional
    public ServiceEntityResponse changeStatus(Long id, ServiceStatusChangeRequest request) {
        validateStatus(request.getStatus());

        Service service = findServiceEntityById(id);
        service.setStatus(request.getStatus());

        return serviceEntityMapper.toResponse(serviceRepository.save(service));
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
