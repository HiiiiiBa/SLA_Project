package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.IncidentCommentCreateRequest;
import com.sla.monitoring.dto.response.IncidentCommentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.IncidentComment;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.IncidentCommentMapper;
import com.sla.monitoring.repository.IncidentCommentRepository;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.IncidentCommentService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentCommentServiceImpl implements IncidentCommentService {

    private final IncidentCommentRepository incidentCommentRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentCommentMapper incidentCommentMapper;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    public List<IncidentCommentResponse> findByIncidentId(Long incidentId) {
        Incident incident = findIncidentWithAccess(incidentId);
        return incidentCommentRepository.findByIncidentIdWithAuthor(incident.getId()).stream()
                .map(incidentCommentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public IncidentCommentResponse addComment(Long incidentId, IncidentCommentCreateRequest request) {
        Incident incident = findIncidentWithAccess(incidentId);
        if (clientScopeService.isCurrentUserClient()) {
            throw new com.sla.monitoring.exception.ForbiddenException("Clients cannot add comments to incidents");
        }
        if (incident.getStatus() == com.sla.monitoring.entity.enums.IncidentStatus.RESOLVED) {
            throw new com.sla.monitoring.exception.BusinessException("Cannot comment on resolved incidents");
        }
        if (employeeScopeService.isCurrentUserEmployee()) {
            employeeScopeService.assertCanManageIncident(incident);
        }

        User author = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", SecurityUtils.getCurrentUserId()));

        IncidentComment comment = IncidentComment.builder()
                .incident(incident)
                .author(author)
                .content(request.getContent().trim())
                .build();

        return incidentCommentMapper.toResponse(incidentCommentRepository.save(comment));
    }

    private Incident findIncidentWithAccess(Long incidentId) {
        Incident incident = incidentRepository.findByIdWithDetails(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));
        employeeScopeService.assertIncidentAccess(incident);
        managerScopeService.assertIncidentAccess(incident);
        clientScopeService.assertIncidentAccess(incident);
        return incident;
    }
}
