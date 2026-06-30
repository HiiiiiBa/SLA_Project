package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.SlaCreateRequest;
import com.sla.monitoring.dto.request.SlaUpdateRequest;
import com.sla.monitoring.dto.response.SlaResponse;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.SlaMapper;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaServiceImpl implements SlaService {

    private final SlaRepository slaRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final SlaMapper slaMapper;

    @Override
    @Transactional
    public SlaResponse createSLA(SlaCreateRequest request) {
        validateSlaMetrics(request.getUptimeTarget(), request.getResponseTimeLimit(), request.getErrorRateLimit());

        Client client = findClientById(request.getClientId());
        Sla sla = slaMapper.toEntity(request);
        sla.setClient(client);

        return enrichResponse(slaRepository.save(sla));
    }

    @Override
    @Transactional
    public SlaResponse updateSLA(Long id, SlaUpdateRequest request) {
        validateSlaMetrics(request.getUptimeTarget(), request.getResponseTimeLimit(), request.getErrorRateLimit());

        Sla sla = findSlaEntityById(id);
        slaMapper.updateEntity(request, sla);
        sla.setClient(findClientById(request.getClientId()));

        return enrichResponse(slaRepository.save(sla));
    }

    @Override
    @Transactional
    public SlaResponse archiveSLA(Long id) {
        Sla sla = findSlaEntityById(id);
        sla.setStatus(SlaStatus.ARCHIVED);
        return enrichResponse(slaRepository.save(sla));
    }

    @Override
    @Transactional
    public SlaResponse activateSLA(Long id) {
        Sla sla = findSlaEntityById(id);
        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            throw new BusinessException("Cannot activate an archived SLA. Restore it manually or create a new contract.");
        }
        sla.setStatus(SlaStatus.ACTIVE);
        return enrichResponse(slaRepository.save(sla));
    }

    @Override
    @Transactional
    public SlaResponse deactivateSLA(Long id) {
        Sla sla = findSlaEntityById(id);
        if (sla.getStatus() == SlaStatus.ARCHIVED) {
            throw new BusinessException("Cannot deactivate an archived SLA");
        }
        sla.setStatus(SlaStatus.INACTIVE);
        return enrichResponse(slaRepository.save(sla));
    }

    @Override
    @Transactional
    public void deleteSLA(Long id) {
        Sla sla = findSlaEntityById(id);
        slaRepository.delete(sla);
    }

    @Override
    public List<SlaResponse> getAll(Long clientId) {
        List<Sla> slas = clientId == null
                ? slaRepository.findAllWithClient()
                : slaRepository.findByClientIdWithClient(clientId);

        return slas.stream()
                .map(this::enrichResponse)
                .toList();
    }

    @Override
    public SlaResponse getById(Long id) {
        Sla sla = slaRepository.findByIdWithClient(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
        return enrichResponse(sla);
    }

    private SlaResponse enrichResponse(Sla sla) {
        SlaResponse response = slaMapper.toResponse(sla);
        response.setServiceCount(serviceRepository.findBySlaId(sla.getId()).size());
        return response;
    }

    private void validateSlaMetrics(Double uptimeTarget, Integer responseTimeLimit, Double errorRateLimit) {
        if (uptimeTarget == null || uptimeTarget < 90 || uptimeTarget > 100) {
            throw new BusinessException("Uptime target must be between 90 and 100");
        }
        if (responseTimeLimit == null || responseTimeLimit <= 0) {
            throw new BusinessException("Response time limit must be greater than 0");
        }
        if (errorRateLimit == null || errorRateLimit < 0) {
            throw new BusinessException("Error rate limit must be greater than or equal to 0");
        }
    }

    private Sla findSlaEntityById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }

    private Client findClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }
}
