package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.IncidentAssignRequest;
import com.sla.monitoring.dto.request.IncidentCreateRequest;
import com.sla.monitoring.dto.request.IncidentStatusChangeRequest;
import com.sla.monitoring.dto.request.IncidentUpdateRequest;
import com.sla.monitoring.dto.response.IncidentResponse;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.IncidentStatus;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.mapper.IncidentMapper;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.IncidentService;
import com.sla.monitoring.service.ManagerScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final SlaRepository slaRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final IncidentMapper incidentMapper;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        Sla sla = findSlaById(request.getSlaId());
        employeeScopeService.assertSlaAccess(sla.getId());
        managerScopeService.assertSlaAccess(sla.getId());
        clientScopeService.assertSlaAccess(sla.getId());
        if (employeeScopeService.isCurrentUserEmployee()) {
            throw new ForbiddenException("Employees cannot create incidents");
        }
        if (request.getProjectId() != null) {
            managerScopeService.assertProjectAccess(request.getProjectId());
            clientScopeService.assertProjectAccess(request.getProjectId());
        }

        Incident incident = incidentMapper.toEntity(request);
        incident.setSla(sla);
        incident.setProject(resolveProject(request.getProjectId()));

        if (request.getAssigneeId() != null) {
            if (clientScopeService.isCurrentUserClient() || employeeScopeService.isCurrentUserEmployee()) {
                throw new ForbiddenException("Only admins and managers can assign incidents");
            }
            incident.setAssignee(resolveAssignee(incident, request.getAssigneeId()));
            incident.setStatus(IncidentStatus.IN_PROGRESS);
        } else {
            incident.setStatus(IncidentStatus.OPEN);
        }

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        Incident incident = findIncidentEntityById(id);
        assertCanMutateIncident(incident);
        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            throw new BusinessException("Resolved incidents cannot be modified");
        }
        validateIncidentDates(request.getStartTime(), request.getEndTime());

        if (employeeScopeService.isCurrentUserEmployee()) {
            incident.setDescription(request.getDescription());
        } else {
            if (request.getProjectId() != null) {
                employeeScopeService.assertProjectAccess(request.getProjectId());
                managerScopeService.assertProjectAccess(request.getProjectId());
                clientScopeService.assertProjectAccess(request.getProjectId());
            }
            incidentMapper.updateEntity(request, incident);
            incident.setProject(resolveProject(request.getProjectId()));
            syncResolvedState(incident, request.getEndTime());
        }

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    private void syncResolvedState(Incident incident, LocalDateTime endTime) {
        if (endTime != null) {
            incident.setEndTime(endTime);
            incident.setStatus(IncidentStatus.RESOLVED);
        }
    }

    @Override
    @Transactional
    public IncidentResponse closeIncident(Long id) {
        return changeStatus(id, IncidentStatusChangeRequest.builder()
                .status(IncidentStatus.RESOLVED)
                .build());
    }

    @Override
    @Transactional
    public IncidentResponse changeStatus(Long id, IncidentStatusChangeRequest request) {
        Incident incident = findIncidentEntityById(id);
        assertCanMutateIncident(incident);
        applyStatusTransition(incident, request.getStatus());
        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse assignIncident(Long id, IncidentAssignRequest request) {
        Incident incident = findIncidentEntityById(id);
        if (clientScopeService.isCurrentUserClient()) {
            throw new ForbiddenException("Clients cannot assign incidents");
        }
        if (employeeScopeService.isCurrentUserEmployee()) {
            throw new ForbiddenException("Only admins and managers can assign incidents");
        }

        Long assigneeId = request.getAssigneeId();
        managerScopeService.assertIncidentAccess(incident);

        if (assigneeId == null) {
            incident.setAssignee(null);
            if (incident.getStatus() == IncidentStatus.IN_PROGRESS) {
                incident.setStatus(IncidentStatus.OPEN);
            }
        } else {
            incident.setAssignee(resolveAssignee(incident, assigneeId));
            if (incident.getStatus() == IncidentStatus.OPEN) {
                incident.setStatus(IncidentStatus.IN_PROGRESS);
            }
        }

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    private void applyStatusTransition(Incident incident, IncidentStatus targetStatus) {
        IncidentStatus currentStatus = incident.getStatus();
        if (currentStatus == targetStatus) {
            return;
        }
        if (currentStatus == IncidentStatus.RESOLVED) {
            throw new BusinessException("Resolved incidents cannot change status");
        }

        if (employeeScopeService.isCurrentUserEmployee()) {
            assertEmployeeStatusTransition(incident, currentStatus, targetStatus);
        }

        switch (targetStatus) {
            case IN_PROGRESS -> {
                if (currentStatus != IncidentStatus.OPEN) {
                    throw new BusinessException("Only open incidents can move to IN_PROGRESS");
                }
                if (incident.getAssignee() == null) {
                    throw new BusinessException("Incident must be assigned before moving to IN_PROGRESS");
                }
                incident.setStatus(IncidentStatus.IN_PROGRESS);
            }
            case RESOLVED -> {
                if (currentStatus != IncidentStatus.IN_PROGRESS) {
                    throw new BusinessException("Only in-progress incidents can be resolved");
                }
                if (employeeScopeService.isCurrentUserEmployee()
                        && (incident.getAssignee() == null
                        || !employeeScopeService.getCurrentUserId().equals(incident.getAssignee().getId()))) {
                    throw new ForbiddenException("Only the assigned employee can resolve this incident");
                }
                incident.setStatus(IncidentStatus.RESOLVED);
                incident.setEndTime(LocalDateTime.now());
            }
            case OPEN -> {
                if (currentStatus != IncidentStatus.IN_PROGRESS) {
                    throw new BusinessException("Only in-progress incidents can return to OPEN");
                }
                incident.setStatus(IncidentStatus.OPEN);
                incident.setAssignee(null);
            }
            default -> throw new BusinessException("Unsupported status transition");
        }
    }

    private void assertEmployeeStatusTransition(
            Incident incident,
            IncidentStatus currentStatus,
            IncidentStatus targetStatus) {
        employeeScopeService.assertCanManageIncident(incident);
        if (targetStatus == IncidentStatus.IN_PROGRESS && incident.getAssignee() == null) {
            throw new BusinessException("Incident must be assigned by a manager before moving to IN_PROGRESS");
        }
        if (targetStatus == IncidentStatus.RESOLVED
                && (incident.getAssignee() == null
                || !employeeScopeService.getCurrentUserId().equals(incident.getAssignee().getId()))) {
            throw new ForbiddenException("Only the assigned employee can resolve this incident");
        }
        if (targetStatus == IncidentStatus.OPEN
                && employeeScopeService.isCurrentUserEmployee()) {
            throw new ForbiddenException("Only managers can unassign incidents");
        }
    }

    @Override
    public List<IncidentResponse> findAll() {
        return filterVisible(incidentRepository.findAllWithDetails()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public IncidentResponse findById(Long id) {
        return incidentMapper.toResponse(findIncidentEntityById(id));
    }

    @Override
    public List<IncidentResponse> findOpenIncidents() {
        return filterVisible(incidentRepository.findByStatusNot(IncidentStatus.RESOLVED)).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySeverity(IncidentSeverity severity) {
        return filterVisible(incidentRepository.findBySeverity(severity)).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findBySlaId(Long slaId) {
        employeeScopeService.assertSlaAccess(slaId);
        managerScopeService.assertSlaAccess(slaId);
        clientScopeService.assertSlaAccess(slaId);
        if (!slaRepository.existsById(slaId)) {
            throw new ResourceNotFoundException("SLA", "id", slaId);
        }
        return filterVisible(incidentRepository.findBySlaId(slaId)).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponse> findByProjectId(Long projectId) {
        employeeScopeService.assertProjectAccess(projectId);
        managerScopeService.assertProjectAccess(projectId);
        clientScopeService.assertProjectAccess(projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return incidentRepository.findByProjectId(projectId).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    private void assertCanMutateIncident(Incident incident) {
        if (clientScopeService.isCurrentUserClient()) {
            throw new ForbiddenException("Clients cannot modify incidents");
        }
        employeeScopeService.assertCanManageIncident(incident);
        managerScopeService.assertIncidentAccess(incident);
    }

    private User resolveAssignee(Incident incident, Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
        Role assignerRole = SecurityUtils.getCurrentUserDetails().getUser().getRole();
        if (assignerRole == Role.ADMIN) {
            return resolveManagerAssignee(incident, assignee);
        }
        if (assignerRole == Role.MANAGER) {
            return resolveEmployeeAssignee(incident, assignee);
        }
        throw new ForbiddenException("You are not allowed to assign incidents");
    }

    private User resolveManagerAssignee(Incident incident, User assignee) {
        if (assignee.getRole() != Role.MANAGER) {
            throw new BusinessException("Admins must assign incidents to a manager");
        }
        Long clientId = resolveIncidentClientId(incident);
        boolean linked = clientRepository.findByIdWithManagers(clientId)
                .map(client -> client.getManagers().stream()
                        .anyMatch(manager -> manager.getId().equals(assignee.getId())))
                .orElse(false);
        if (!linked) {
            throw new BusinessException("Assignee must be a manager for this client");
        }
        return assignee;
    }

    private User resolveEmployeeAssignee(Incident incident, User assignee) {
        if (assignee.getRole() != Role.EMPLOYEE) {
            throw new BusinessException("Managers must assign incidents to an employee");
        }
        if (incident.getProject() != null) {
            Project project = projectRepository.findByIdWithDetails(incident.getProject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", incident.getProject().getId()));
            boolean member = project.getAssignedMembers().stream()
                    .anyMatch(user -> user.getId().equals(assignee.getId()));
            if (!member) {
                throw new BusinessException("Assignee must be a member of the incident project");
            }
        } else {
            employeeScopeService.assertSlaAccess(incident.getSla().getId());
        }
        return assignee;
    }

    private Long resolveIncidentClientId(Incident incident) {
        if (incident.getProject() != null) {
            Project project = projectRepository.findById(incident.getProject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", incident.getProject().getId()));
            return project.getClient().getId();
        }
        Sla sla = incident.getSla();
        if (sla.getClient() == null) {
            Long slaId = sla.getId();
            sla = slaRepository.findById(slaId)
                    .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", slaId));
        }
        return sla.getClient().getId();
    }

    private List<Incident> filterVisible(List<Incident> incidents) {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return incidents.stream()
                    .filter(employeeScopeService::isIncidentVisible)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            return incidents.stream()
                    .filter(managerScopeService::isIncidentVisible)
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            return incidents.stream()
                    .filter(clientScopeService::isIncidentVisible)
                    .toList();
        }
        return incidents;
    }

    private void validateIncidentDates(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException("End time must be after start time");
        }
    }

    private Incident findIncidentEntityById(Long id) {
        Incident incident = incidentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        employeeScopeService.assertIncidentAccess(incident);
        managerScopeService.assertIncidentAccess(incident);
        clientScopeService.assertIncidentAccess(incident);
        return incident;
    }

    private Sla findSlaById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", id));
    }

    private Project resolveProject(Long projectId) {
        if (projectId == null) {
            return null;
        }
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }
}
