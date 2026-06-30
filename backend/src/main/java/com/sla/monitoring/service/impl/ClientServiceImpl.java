package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.ClientCreateRequest;
import com.sla.monitoring.dto.request.ClientUpdateRequest;
import com.sla.monitoring.dto.response.ClientResponse;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.ClientMapper;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        Client client = clientMapper.toEntity(request);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = findClientEntityById(id);
        clientMapper.updateEntity(request, client);
        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = findClientEntityById(id);
        clientRepository.delete(client);
    }

    @Override
    public ClientResponse getClient(Long id) {
        return clientMapper.toResponse(findClientEntityById(id));
    }

    @Override
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    private Client findClientEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));
    }
}
