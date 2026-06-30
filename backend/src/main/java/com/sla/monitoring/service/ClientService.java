package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ClientCreateRequest;
import com.sla.monitoring.dto.request.ClientUpdateRequest;
import com.sla.monitoring.dto.response.ClientPortfolioResponse;
import com.sla.monitoring.dto.response.ClientResponse;

import java.util.List;

public interface ClientService {

    ClientResponse createClient(ClientCreateRequest request);

    ClientResponse updateClient(Long id, ClientUpdateRequest request);

    void deleteClient(Long id);

    ClientResponse getClient(Long id);

    List<ClientResponse> getAllClients();

    ClientPortfolioResponse getClientPortfolio(Long id);
}
