package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.IncidentCommentCreateRequest;
import com.sla.monitoring.dto.response.IncidentCommentResponse;

import java.util.List;

public interface IncidentCommentService {

    List<IncidentCommentResponse> findByIncidentId(Long incidentId);

    IncidentCommentResponse addComment(Long incidentId, IncidentCommentCreateRequest request);
}
