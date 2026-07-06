package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.TeamCreateRequest;
import com.sla.monitoring.dto.request.TeamUpdateRequest;
import com.sla.monitoring.dto.response.TeamResponse;

import java.util.List;

public interface TeamService {

    List<TeamResponse> findAll();

    List<TeamResponse> findByManagerId(Long managerId);

    TeamResponse findById(Long id);

    TeamResponse create(TeamCreateRequest request);

    TeamResponse update(Long id, TeamUpdateRequest request);

    void delete(Long id);
}
