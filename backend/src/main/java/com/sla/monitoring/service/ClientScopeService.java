package com.sla.monitoring.service;

import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Restricts CLIENT users to the client record linked to their account email.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientScopeService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final SlaRepository slaRepository;

    public boolean isCurrentUserClient() {
        return SecurityUtils.getCurrentUserDetails().getUser().getRole() == Role.CLIENT;
    }

    public Client getLinkedClient() {
        String email = SecurityUtils.getCurrentUserEmail();
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("No client profile linked to this account"));
    }

    public Long getClientId() {
        return getLinkedClient().getId();
    }

    public Set<Long> getScopedProjectIds() {
        Set<Long> ids = new HashSet<>();
        projectRepository.findByClientId(getClientId()).forEach(p -> ids.add(p.getId()));
        return ids;
    }

    public Set<Long> getScopedSlaIds() {
        Set<Long> ids = new HashSet<>();
        slaRepository.findByClientId(getClientId()).forEach(sla -> ids.add(sla.getId()));
        return ids;
    }

    public Set<Long> getVisibleTeamIds() {
        Set<Long> ids = new HashSet<>();
        projectRepository.findByClientId(getClientId()).stream()
                .filter(project -> project.getTeam() != null)
                .forEach(project -> ids.add(project.getTeam().getId()));
        return ids;
    }

    public void assertTeamAccess(Long teamId) {
        if (!isCurrentUserClient()) {
            return;
        }
        if (!getVisibleTeamIds().contains(teamId)) {
            throw new ForbiddenException("Access denied to this team");
        }
    }

    public void assertClientAccess(Long clientId) {
        if (!isCurrentUserClient()) {
            return;
        }
        if (!getClientId().equals(clientId)) {
            throw new ForbiddenException("Access denied to this client");
        }
    }

    public void assertProjectAccess(Long projectId) {
        if (!isCurrentUserClient()) {
            return;
        }
        if (!getScopedProjectIds().contains(projectId)) {
            throw new ForbiddenException("Access denied to this project");
        }
    }

    public void assertSlaAccess(Long slaId) {
        if (!isCurrentUserClient()) {
            return;
        }
        if (!getScopedSlaIds().contains(slaId)) {
            throw new ForbiddenException("Access denied to this SLA");
        }
    }

    public boolean isIncidentVisible(Incident incident) {
        if (!isCurrentUserClient()) {
            return true;
        }
        if (incident.getProject() != null) {
            return getScopedProjectIds().contains(incident.getProject().getId());
        }
        return incident.getSla() != null
                && getClientId().equals(incident.getSla().getClient().getId());
    }

    public void assertIncidentAccess(Incident incident) {
        if (!isIncidentVisible(incident)) {
            throw new ForbiddenException("Access denied to this incident");
        }
    }

    public List<Client> getAssignedClients() {
        return List.of(getLinkedClient());
    }
}
