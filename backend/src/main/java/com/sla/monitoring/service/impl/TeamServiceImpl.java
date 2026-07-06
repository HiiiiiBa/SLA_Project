package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.TeamCreateRequest;
import com.sla.monitoring.dto.request.TeamUpdateRequest;
import com.sla.monitoring.dto.response.TeamMemberResponse;
import com.sla.monitoring.dto.response.TeamResponse;
import com.sla.monitoring.entity.Team;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.TeamRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.service.ClientScopeService;
import com.sla.monitoring.service.EmployeeScopeService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeScopeService employeeScopeService;
    private final ManagerScopeService managerScopeService;
    private final ClientScopeService clientScopeService;

    @Override
    public List<TeamResponse> findAll() {
        if (employeeScopeService.isCurrentUserEmployee()) {
            return teamRepository.findByMemberId(employeeScopeService.getCurrentUserId()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (clientScopeService.isCurrentUserClient()) {
            Set<Long> teamIds = clientScopeService.getVisibleTeamIds();
            if (teamIds.isEmpty()) {
                return List.of();
            }
            return teamRepository.findAllWithDetails().stream()
                    .filter(team -> teamIds.contains(team.getId()))
                    .map(this::toResponse)
                    .toList();
        }
        if (managerScopeService.isCurrentUserManager()) {
            Set<Long> teamIds = managerScopeService.getVisibleTeamIds();
            if (teamIds.isEmpty()) {
                return List.of();
            }
            return teamRepository.findAllWithDetails().stream()
                    .filter(team -> teamIds.contains(team.getId()))
                    .map(this::toResponse)
                    .toList();
        }
        return teamRepository.findAllWithDetails().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<TeamResponse> findByManagerId(Long managerId) {
        return teamRepository.findByManagerId(managerId).stream()
                .map(team -> teamRepository.findByIdWithDetails(team.getId()).orElse(team))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TeamResponse findById(Long id) {
        employeeScopeService.assertTeamAccess(id);
        managerScopeService.assertTeamAccess(id);
        clientScopeService.assertTeamAccess(id);
        return toResponse(findTeamEntity(id));
    }

    @Override
    @Transactional
    public TeamResponse create(TeamCreateRequest request) {
        User manager = findManager(request.getManagerId());
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .manager(manager)
                .members(resolveMembers(request.getMemberIds()))
                .build();
        return toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public TeamResponse update(Long id, TeamUpdateRequest request) {
        Team team = findTeamEntity(id);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setManager(findManager(request.getManagerId()));
        team.getMembers().clear();
        team.getMembers().addAll(resolveMembers(request.getMemberIds()));
        return toResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Team team = findTeamEntity(id);
        if (projectRepository.findByTeamId(id).stream().findAny().isPresent()) {
            throw new BusinessException("Cannot delete a team that still has assigned projects");
        }
        teamRepository.delete(team);
    }

    private Team findTeamEntity(Long id) {
        return teamRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
    }

    private User findManager(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));
        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
            throw new BusinessException("Team manager must have MANAGER or ADMIN role");
        }
        return manager;
    }

    private Set<User> resolveMembers(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<User> members = new HashSet<>();
        for (Long memberId : memberIds) {
            User user = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", memberId));
            if (user.getRole() != Role.EMPLOYEE) {
                throw new BusinessException("Team members must be EMPLOYEE accounts");
            }
            members.add(user);
        }
        return members;
    }

    private TeamResponse toResponse(Team team) {
        List<TeamMemberResponse> members = team.getMembers().stream()
                .map(user -> TeamMemberResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .build())
                .toList();

        User manager = team.getManager();
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .managerId(manager.getId())
                .managerName(manager.getFirstName() + " " + manager.getLastName())
                .members(members)
                .memberCount(members.size())
                .projectCount(projectRepository.findByTeamId(team.getId()).size())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
