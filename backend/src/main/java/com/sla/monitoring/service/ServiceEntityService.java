package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ServiceEntityCreateRequest;
import com.sla.monitoring.dto.request.ServiceEntityUpdateRequest;
import com.sla.monitoring.dto.request.ServiceStatusChangeRequest;
import com.sla.monitoring.dto.response.ServiceEntityResponse;

import java.util.List;

public interface ServiceEntityService {

    ServiceEntityResponse createService(ServiceEntityCreateRequest request);

    ServiceEntityResponse updateService(Long id, ServiceEntityUpdateRequest request);

    void deleteService(Long id);

    List<ServiceEntityResponse> findAll(Long slaId);

    List<ServiceEntityResponse> findBySlaId(Long slaId);

    ServiceEntityResponse findById(Long id);

    ServiceEntityResponse changeStatus(Long id, ServiceStatusChangeRequest request);
}
