package com.sla.monitoring.service.impl;

import com.sla.monitoring.dto.request.ApprovalRequestCreateRequest;
import com.sla.monitoring.dto.request.ApprovalReviewRequest;
import com.sla.monitoring.dto.response.ApprovalRequestResponse;
import com.sla.monitoring.entity.ApprovalRequest;
import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.Team;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.ApprovalActionType;
import com.sla.monitoring.entity.enums.ApprovalRequestStatus;
import com.sla.monitoring.entity.enums.ApprovalTargetType;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.exception.BusinessException;
import com.sla.monitoring.exception.ForbiddenException;
import com.sla.monitoring.exception.ResourceNotFoundException;
import com.sla.monitoring.notification.ApprovalNotificationService;
import com.sla.monitoring.repository.ApprovalRequestRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.repository.TeamRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.security.util.SecurityUtils;
import com.sla.monitoring.service.ApprovalRequestService;
import com.sla.monitoring.service.ManagerScopeService;
import com.sla.monitoring.service.ProjectService;
import com.sla.monitoring.service.SlaService;
import com.sla.monitoring.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final SlaRepository slaRepository;
    private final ManagerScopeService managerScopeService;
    private final ProjectService projectService;
    private final TeamService teamService;
    private final SlaService slaService;
    private final ApprovalNotificationService approvalNotificationService;

    @Override
    @Transactional
    public ApprovalRequestResponse submit(ApprovalRequestCreateRequest request) {
        assertManagerRole();
        validateActionTargetMatch(request.getActionType(), request.getTargetType());
        String targetLabel = resolveTargetLabel(request.getTargetType(), request.getTargetId());
        assertManagerScope(request.getActionType(), request.getTargetType(), request.getTargetId());

        if (approvalRequestRepository.existsByActionTypeAndTargetTypeAndTargetIdAndStatus(
                request.getActionType(),
                request.getTargetType(),
                request.getTargetId(),
                ApprovalRequestStatus.PENDING)) {
            throw new BusinessException("Une demande identique est déjà en attente de validation");
        }

        User requester = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", SecurityUtils.getCurrentUserId()));

        ApprovalRequest entity = ApprovalRequest.builder()
                .requester(requester)
                .actionType(request.getActionType())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .targetLabel(targetLabel)
                .reason(request.getReason())
                .status(ApprovalRequestStatus.PENDING)
                .build();

        ApprovalRequest saved = approvalRequestRepository.save(entity);
        approvalNotificationService.notifyAdminsOfSubmission(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse approve(Long id, ApprovalReviewRequest review) {
        assertAdminRole();
        ApprovalRequest request = findEntity(id);
        if (request.getStatus() != ApprovalRequestStatus.PENDING) {
            throw new BusinessException("Cette demande n'est plus en attente");
        }

        User reviewer = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", SecurityUtils.getCurrentUserId()));
        request.setReviewer(reviewer);
        request.setReviewComment(review != null ? review.getComment() : null);
        request.setReviewedAt(LocalDateTime.now());
        request.setStatus(ApprovalRequestStatus.APPROVED);

        try {
            executeAction(request);
            request.setStatus(ApprovalRequestStatus.EXECUTED);
            request.setExecutedAt(LocalDateTime.now());
        } catch (RuntimeException ex) {
            request.setStatus(ApprovalRequestStatus.FAILED);
            request.setReviewComment(appendComment(request.getReviewComment(), ex.getMessage()));
            ApprovalRequest saved = approvalRequestRepository.save(request);
            approvalNotificationService.notifyRequesterRejected(saved);
            throw new BusinessException("Exécution impossible : " + ex.getMessage());
        }

        ApprovalRequest saved = approvalRequestRepository.save(request);
        approvalNotificationService.notifyRequesterApproved(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse reject(Long id, ApprovalReviewRequest review) {
        assertAdminRole();
        ApprovalRequest request = findEntity(id);
        if (request.getStatus() != ApprovalRequestStatus.PENDING) {
            throw new BusinessException("Cette demande n'est plus en attente");
        }

        User reviewer = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", SecurityUtils.getCurrentUserId()));
        request.setReviewer(reviewer);
        request.setReviewComment(review != null ? review.getComment() : null);
        request.setReviewedAt(LocalDateTime.now());
        request.setStatus(ApprovalRequestStatus.REJECTED);

        ApprovalRequest saved = approvalRequestRepository.save(request);
        approvalNotificationService.notifyRequesterRejected(saved);
        return toResponse(saved);
    }

    @Override
    public List<ApprovalRequestResponse> findPending() {
        assertAdminRole();
        return approvalRequestRepository.findByStatusWithUsers(ApprovalRequestStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ApprovalRequestResponse> findMine() {
        assertManagerRole();
        return approvalRequestRepository.findByRequesterIdWithUsers(SecurityUtils.getCurrentUserId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ApprovalRequestResponse findById(Long id) {
        ApprovalRequest request = findEntity(id);
        Role role = SecurityUtils.getCurrentUserDetails().getUser().getRole();
        if (role == Role.ADMIN) {
            return toResponse(request);
        }
        if (role == Role.MANAGER && request.getRequester().getId().equals(SecurityUtils.getCurrentUserId())) {
            return toResponse(request);
        }
        throw new ForbiddenException("Access denied to this approval request");
    }

    private void executeAction(ApprovalRequest request) {
        switch (request.getActionType()) {
            case DELETE_PROJECT -> projectService.delete(request.getTargetId());
            case DELETE_TEAM -> teamService.delete(request.getTargetId());
            case DELETE_SLA -> slaService.deleteSLA(request.getTargetId());
            case ARCHIVE_SLA -> slaService.archiveSLA(request.getTargetId());
            case ACTIVATE_SLA -> slaService.activateSLA(request.getTargetId());
            case DEACTIVATE_SLA -> slaService.deactivateSLA(request.getTargetId());
        }
    }

    private void assertManagerScope(
            ApprovalActionType actionType,
            ApprovalTargetType targetType,
            Long targetId) {
        switch (targetType) {
            case PROJECT -> managerScopeService.assertProjectAccess(targetId);
            case TEAM -> managerScopeService.assertTeamAccess(targetId);
            case SLA -> managerScopeService.assertSlaAccess(targetId);
        }
        if (actionType == ApprovalActionType.DELETE_TEAM) {
            teamRepository.findById(targetId).ifPresent(team -> {
                if (projectRepository.findByTeamId(targetId).stream().findAny().isPresent()) {
                    throw new BusinessException("Impossible de demander la suppression d'une équipe encore assignée à un projet");
                }
            });
        }
    }

    private String resolveTargetLabel(ApprovalTargetType targetType, Long targetId) {
        return switch (targetType) {
            case PROJECT -> projectRepository.findById(targetId)
                    .map(Project::getName)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", targetId));
            case TEAM -> teamRepository.findById(targetId)
                    .map(Team::getName)
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", targetId));
            case SLA -> slaRepository.findById(targetId)
                    .map(Sla::getName)
                    .orElseThrow(() -> new ResourceNotFoundException("SLA", "id", targetId));
        };
    }

    private void validateActionTargetMatch(ApprovalActionType actionType, ApprovalTargetType targetType) {
        boolean valid = switch (actionType) {
            case DELETE_PROJECT -> targetType == ApprovalTargetType.PROJECT;
            case DELETE_TEAM -> targetType == ApprovalTargetType.TEAM;
            case DELETE_SLA, ARCHIVE_SLA, ACTIVATE_SLA, DEACTIVATE_SLA -> targetType == ApprovalTargetType.SLA;
        };
        if (!valid) {
            throw new BusinessException("Action incompatible avec le type de cible");
        }
    }

    private ApprovalRequest findEntity(Long id) {
        return approvalRequestRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", "id", id));
    }

    private void assertManagerRole() {
        if (!managerScopeService.isCurrentUserManager()) {
            throw new ForbiddenException("Only managers can submit approval requests");
        }
    }

    private void assertAdminRole() {
        if (SecurityUtils.getCurrentUserDetails().getUser().getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can review approval requests");
        }
    }

    private String appendComment(String existing, String extra) {
        if (existing == null || existing.isBlank()) {
            return extra;
        }
        return existing + " — " + extra;
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest request) {
        return ApprovalRequestResponse.builder()
                .id(request.getId())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getFirstName() + " " + request.getRequester().getLastName())
                .requesterEmail(request.getRequester().getEmail())
                .actionType(request.getActionType())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .targetLabel(request.getTargetLabel())
                .reason(request.getReason())
                .status(request.getStatus())
                .reviewerId(request.getReviewer() != null ? request.getReviewer().getId() : null)
                .reviewerName(request.getReviewer() != null
                        ? request.getReviewer().getFirstName() + " " + request.getReviewer().getLastName()
                        : null)
                .reviewComment(request.getReviewComment())
                .reviewedAt(request.getReviewedAt())
                .executedAt(request.getExecutedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
