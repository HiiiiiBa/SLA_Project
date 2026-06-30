package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortfolioResponse {

    private ClientResponse client;
    private List<SlaWithServicesResponse> slas;
}
