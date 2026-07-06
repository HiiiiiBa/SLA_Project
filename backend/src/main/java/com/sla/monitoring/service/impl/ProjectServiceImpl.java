package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.ProjectCreateRequest;
import com.sla.monitoring.dto.request.ProjectUpdateRequest;
import com.sla.monitoring.dto.response.ProjectResponse;
import com.sla.monitoring.dto.response.TeamMemberResponse;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.Team;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.ProjectStatus;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.TeamRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    public List<ProjectResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return employeeScopeService.getAssignedProjects().stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            return projectRepository.findByClientId(clientScopeService.getClientId()).stream()
                    .map(project -> projectRepository.findByIdWithDetails(project.getId()).orElse(project))
                    .map(this::toResponse)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            return projectRepository.findAllWithDetails().stream()
                    .filter(p -> managerScopeService.getAssignedClientIds().contains(p.getClient().getId()))
                    .map(this::toResponse)
                    .toList();
        }
        return projectRepository.findAllWithDetails().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProjectResponse> findByClientId(Long clientId) {
        employeeScopeService.assertClientAccess(clientId);
        managerScopeService.assertClientAccess(clientId);
        clientScopeService.assertClientAccess(clientId);
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }
        return projectRepository.findByClientId(clientId).stream()
                .map(project -> projectRepository.findByIdWithDetails(project.getId()).orElse(project))
                .filter(project -> !employeeScopeService.isCurrentUserEmployee()
                        || employeeScopeService.getAssignedProjectIds().contains(project.getId()))
                .filter(project -> !managerScopeService.isCurrentUserManager()
                        || managerScopeService.getAssignedClientIds().contains(project.getClient().getId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProjectResponse> findByTeamId(Long teamId) {
        employeeScopeService.assertTeamAccess(teamId);
        managerScopeService.assertTeamAccess(teamId);
        clientScopeService.assertTeamAccess(teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team", "id", teamId);
        }
        return projectRepository.findByTeamId(teamId).stream()
                .map(project -> projectRepository.findByIdWithDetails(project.getId()).orElse(project))
                .filter(project -> !employeeScopeService.isCurrentUserEmployee()
                        || employeeScopeService.getAssignedProjectIds().contains(project.getId()))
                .filter(project -> !managerScopeService.isCurrentUserManager()
                        || managerScopeService.getAssignedClientIds().contains(project.getClient().getId()))
                .filter(project -> !clientScopeService.isCurrentUserClient()
                        || clientScopeService.getScopedProjectIds().contains(project.getId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ProjectResponse findById(Long id) {
        Project project = findProjectEntity(id);
        employeeScopeService.assertProjectAccess(id);
        managerScopeService.assertProjectAccess(id);
        clientScopeService.assertProjectAccess(id);
        return toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        managerScopeService.assertClientAccess(request.getClientId());
        Client client = findClient(request.getClientId());
        Team team = resolveTeam(request.getTeamId());
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(ProjectStatus.ACTIVE)
                .client(client)
                .team(team)
                .assignedMembers(resolveMembers(request.getMemberIds(), team))
                .build();
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        managerScopeService.assertProjectAccess(id);
        Project project = findProjectEntity(id);
        managerScopeService.assertClientAccess(request.getClientId());
        Team team = resolveTeam(request.getTeamId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setClient(findClient(request.getClientId()));
        project.setTeam(team);
        project.getAssignedMembers().clear();
        project.getAssignedMembers().addAll(resolveMembers(request.getMemberIds(), team));
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        managerScopeService.assertProjectAccess(id);
        projectRepository.delete(findProjectEntity(id));
    }

    private Project findProjectEntity(Long id) {
        return projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    private Client findClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
    }

    private Team resolveTeam(Long teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
    }

    private Set<User> resolveMembers(List<Long> memberIds, Team team) {
        if (memberIds == null || memberIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<User> members = new HashSet<>();
        for (Long memberId : memberIds) {
            User user = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", memberId));
            if (user.getRole() != Role.EMPLOYEE) {
                throw new BusinessException("Project members must be EMPLOYEE accounts");
            }
            if (team != null && !team.getMembers().contains(user)) {
                throw new BusinessException("Assigned employee must belong to the selected team");
            }
            members.add(user);
        }
        return members;
    }

    private ProjectResponse toResponse(Project project) {
        List<TeamMemberResponse> members = project.getAssignedMembers().stream()
                .map(user -> TeamMemberResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build())
                .toList();

        Team team = project.getTeam();
        Sla sla = project.getSla();
        String managerName = team != null && team.getManager() != null
                ? team.getManager().getFirstName() + " " + team.getManager().getLastName()
                : null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .clientId(project.getClient().getId())
                .clientName(project.getClient().getName())
                .teamId(team != null ? team.getId() : null)
                .teamName(team != null ? team.getName() : null)
                .slaId(sla != null ? sla.getId() : null)
                .slaName(sla != null ? sla.getName() : null)
                .managerName(managerName)
                .assignedMembers(members)
                .memberCount(members.size())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
