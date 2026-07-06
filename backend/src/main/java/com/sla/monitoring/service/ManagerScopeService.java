package com.sla.monitoring.service;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.repository.TeamRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Restricts MANAGER users to clients explicitly assigned to them.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerScopeService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final SlaRepository slaRepository;
    private final TeamRepository teamRepository;

    public boolean isCurrentUserManager() {
        return SecurityUtils.getCurrentUserDetails().getUser().getRole() == Role.MANAGER;
    }

    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    public Set<Long> getAssignedClientIds() {
        return new HashSet<>(clientRepository.findClientIdsByManagerId(getCurrentUserId()));
    }

    public Set<Long> getScopedProjectIds() {
        Set<Long> ids = new HashSet<>();
        for (Long clientId : getAssignedClientIds()) {
            projectRepository.findByClientId(clientId).forEach(p -> ids.add(p.getId()));
        }
        return ids;
    }

    public Set<Long> getScopedSlaIds() {
        Set<Long> ids = new HashSet<>();
        Set<Long> clientIds = getAssignedClientIds();
        if (clientIds.isEmpty()) {
            return ids;
        }
        slaRepository.findByClientIdIn(clientIds).forEach(sla -> ids.add(sla.getId()));
        return ids;
    }

    public Set<Long> getManagedTeamIds() {
        Set<Long> ids = new HashSet<>();
        teamRepository.findByManagerId(getCurrentUserId()).forEach(team -> ids.add(team.getId()));
        return ids;
    }

    /** Teams the manager owns or that support projects on assigned clients. */
    public Set<Long> getVisibleTeamIds() {
        Set<Long> ids = new HashSet<>(getManagedTeamIds());
        for (Long clientId : getAssignedClientIds()) {
            projectRepository.findByClientId(clientId).stream()
                    .filter(project -> project.getTeam() != null)
                    .forEach(project -> ids.add(project.getTeam().getId()));
        }
        return ids;
    }

    public void assertClientAccess(Long clientId) {
        if (!isCurrentUserManager()) {
            return;
        }
        if (!getAssignedClientIds().contains(clientId)) {
            throw new ForbiddenException("Access denied to this client");
        }
    }

    public void assertProjectAccess(Long projectId) {
        if (!isCurrentUserManager()) {
            return;
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ForbiddenException("Access denied to this project"));
        assertClientAccess(project.getClient().getId());
    }

    public void assertSlaAccess(Long slaId) {
        if (!isCurrentUserManager()) {
            return;
        }
        if (!getScopedSlaIds().contains(slaId)) {
            throw new ForbiddenException("Access denied to this SLA");
        }
    }

    public void assertTeamAccess(Long teamId) {
        if (!isCurrentUserManager()) {
            return;
        }
        if (!getVisibleTeamIds().contains(teamId)) {
            throw new ForbiddenException("Access denied to this team");
        }
    }

    public boolean isIncidentVisible(Incident incident) {
        if (!isCurrentUserManager()) {
            return true;
        }
        if (incident.getProject() != null) {
            return getScopedProjectIds().contains(incident.getProject().getId());
        }
        return incident.getSla() != null
                && getAssignedClientIds().contains(incident.getSla().getClient().getId());
    }

    public void assertIncidentAccess(Incident incident) {
        if (!isIncidentVisible(incident)) {
            throw new ForbiddenException("Access denied to this incident");
        }
    }

    public List<com.sla.monitoring.entity.Client> getAssignedClients() {
        return clientRepository.findByManagerId(getCurrentUserId());
    }
}
