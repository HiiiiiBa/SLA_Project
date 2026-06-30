package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.SlaCreateRequest;
import com.sla.monitoring.dto.request.SlaUpdateRequest;
import com.sla.monitoring.dto.response.SlaResponse;

import java.util.List;

public interface SlaService {

    SlaResponse createSLA(SlaCreateRequest request);

    SlaResponse updateSLA(Long id, SlaUpdateRequest request);

    SlaResponse archiveSLA(Long id);

    SlaResponse activateSLA(Long id);

    SlaResponse deactivateSLA(Long id);

    void deleteSLA(Long id);

    List<SlaResponse> getAll(Long clientId);

    SlaResponse getById(Long id);
}
