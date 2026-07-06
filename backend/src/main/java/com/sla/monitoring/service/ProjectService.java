package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.ProjectCreateRequest;
import com.sla.monitoring.dto.request.ProjectUpdateRequest;
import com.sla.monitoring.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {

    List<ProjectResponse> findAll();

    List<ProjectResponse> findByClientId(Long clientId);

    List<ProjectResponse> findByTeamId(Long teamId);

    ProjectResponse findById(Long id);

    ProjectResponse create(ProjectCreateRequest request);

    ProjectResponse update(Long id, ProjectUpdateRequest request);

    void delete(Long id);
}
