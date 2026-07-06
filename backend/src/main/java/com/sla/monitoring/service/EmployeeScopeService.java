package com.sla.monitoring.service;

import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Team;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.TeamRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Restricts read access for EMPLOYEE users to their assigned projects, teams and related SLA data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeScopeService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    public boolean isCurrentUserEmployee() {
        return SecurityUtils.getCurrentUserDetails().getUser().getRole() == Role.EMPLOYEE;
    }

    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    public List<Project> getAssignedProjects() {
        return projectRepository.findByAssignedMemberId(getCurrentUserId());
    }

    public Set<Long> getAssignedProjectIds() {
        Set<Long> ids = new HashSet<>();
        for (Project project : getAssignedProjects()) {
            ids.add(project.getId());
        }
        return ids;
    }

    public Set<Long> getScopedClientIds() {
        Set<Long> ids = new HashSet<>();
        for (Project project : getAssignedProjects()) {
            ids.add(project.getClient().getId());
        }
        return ids;
    }

    public Set<Long> getMemberTeamIds() {
        Set<Long> ids = new HashSet<>();
        for (Team team : teamRepository.findByMemberId(getCurrentUserId())) {
            ids.add(team.getId());
        }
        return ids;
    }

    public Set<Long> getScopedSlaIds() {
        Set<Long> slaIds = new HashSet<>();
        for (Project project : getAssignedProjects()) {
            if (project.getSla() != null) {
                slaIds.add(project.getSla().getId());
            }
        }
        return slaIds;
    }

    public void assertProjectAccess(Long projectId) {
        if (!isCurrentUserEmployee()) {
            return;
        }
        if (!getAssignedProjectIds().contains(projectId)) {
            throw new ForbiddenException("Access denied to this project");
        }
    }

    public void assertTeamAccess(Long teamId) {
        if (!isCurrentUserEmployee()) {
            return;
        }
        if (!getMemberTeamIds().contains(teamId)) {
            throw new ForbiddenException("Access denied to this team");
        }
    }

    public void assertSlaAccess(Long slaId) {
        if (!isCurrentUserEmployee()) {
            return;
        }
        if (!getScopedSlaIds().contains(slaId)) {
            throw new ForbiddenException("Access denied to this SLA");
        }
    }

    public void assertClientAccess(Long clientId) {
        if (!isCurrentUserEmployee()) {
            return;
        }
        if (!getScopedClientIds().contains(clientId)) {
            throw new ForbiddenException("Access denied to this client");
        }
    }

    public boolean isIncidentVisible(Incident incident) {
        if (!isCurrentUserEmployee()) {
            return true;
        }
        if (incident.getAssignee() == null) {
            return false;
        }
        return getCurrentUserId().equals(incident.getAssignee().getId());
    }

    public void assertIncidentAccess(Incident incident) {
        if (!isIncidentVisible(incident)) {
            throw new ForbiddenException("Access denied to this incident");
        }
    }

    public boolean canManageIncident(Incident incident) {
        if (!isCurrentUserEmployee()) {
            return true;
        }
        if (incident.getAssignee() == null) {
            return false;
        }
        return getCurrentUserId().equals(incident.getAssignee().getId());
    }

    public void assertCanManageIncident(Incident incident) {
        assertIncidentAccess(incident);
        if (!canManageIncident(incident)) {
            throw new ForbiddenException("This incident is assigned to another user");
        }
    }
}
