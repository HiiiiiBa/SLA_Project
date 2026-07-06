package com.sla.monitoring.notification;

import com.sla.monitoring.config.AlertNotificationProperties;
import com.sla.monitoring.entity.ApprovalRequest;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.notification.dto.ApprovalNotificationMessage;
import com.sla.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalNotificationServiceImpl implements ApprovalNotificationService {

    public static final String USER_APPROVALS_DESTINATION = "/queue/approvals";

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final AlertNotificationProperties properties;

    @Override
    @Async
    public void notifyAdminsOfSubmission(ApprovalRequest request) {
        if (!properties.isWebsocketEnabled()) {
            return;
        }
        ApprovalNotificationMessage message = toMessage(
                request,
                ApprovalNotificationMessage.KIND_SUBMITTED,
                buildSubmittedMessage(request));
        List<User> admins = userRepository.findByRoleAndEnabledTrue(Role.ADMIN);
        for (User admin : admins) {
            sendToUser(admin.getEmail(), message);
        }
    }

    @Override
    @Async
    public void notifyRequesterApproved(ApprovalRequest request) {
        if (!properties.isWebsocketEnabled()) {
            return;
        }
        ApprovalNotificationMessage message = toMessage(
                request,
                ApprovalNotificationMessage.KIND_APPROVED,
                buildApprovedMessage(request));
        sendToUser(request.getRequester().getEmail(), message);
    }

    @Override
    @Async
    public void notifyRequesterRejected(ApprovalRequest request) {
        if (!properties.isWebsocketEnabled()) {
            return;
        }
        ApprovalNotificationMessage message = toMessage(
                request,
                ApprovalNotificationMessage.KIND_REJECTED,
                buildRejectedMessage(request));
        sendToUser(request.getRequester().getEmail(), message);
    }

    private void sendToUser(String email, ApprovalNotificationMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(email, USER_APPROVALS_DESTINATION, message);
        } catch (Exception ex) {
            log.warn("Failed to send approval notification to {}: {}", email, ex.getMessage());
        }
    }

    private ApprovalNotificationMessage toMessage(
            ApprovalRequest request,
            String kind,
            String messageText) {
        String requesterName = formatUserName(request.getRequester());
        String reviewerName = request.getReviewer() != null ? formatUserName(request.getReviewer()) : null;
        return ApprovalNotificationMessage.builder()
                .requestId(request.getId())
                .kind(kind)
                .actionType(request.getActionType())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .targetLabel(request.getTargetLabel())
                .message(messageText)
                .status(request.getStatus())
                .requesterName(requesterName)
                .reviewerName(reviewerName)
                .reviewComment(request.getReviewComment())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private String buildSubmittedMessage(ApprovalRequest request) {
        return requesterLabel(request) + " demande : " + actionLabel(request.getActionType())
                + " — " + request.getTargetLabel();
    }

    private String buildApprovedMessage(ApprovalRequest request) {
        String reviewer = request.getReviewer() != null ? formatUserName(request.getReviewer()) : "Admin";
        return "Votre demande a été approuvée par " + reviewer + " : "
                + actionLabel(request.getActionType()) + " — " + request.getTargetLabel();
    }

    private String buildRejectedMessage(ApprovalRequest request) {
        String reviewer = request.getReviewer() != null ? formatUserName(request.getReviewer()) : "Admin";
        String base = "Votre demande a été refusée par " + reviewer + " : "
                + actionLabel(request.getActionType()) + " — " + request.getTargetLabel();
        if (request.getReviewComment() != null && !request.getReviewComment().isBlank()) {
            return base + " — Motif : " + request.getReviewComment();
        }
        return base;
    }

    private String requesterLabel(ApprovalRequest request) {
        return formatUserName(request.getRequester());
    }

    private String formatUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String actionLabel(com.sla.monitoring.entity.enums.ApprovalActionType actionType) {
        return switch (actionType) {
            case DELETE_PROJECT -> "Suppression de projet";
            case DELETE_TEAM -> "Suppression d'équipe";
            case DELETE_SLA -> "Suppression de SLA";
            case ARCHIVE_SLA -> "Archivage de SLA";
            case ACTIVATE_SLA -> "Activation de SLA";
            case DEACTIVATE_SLA -> "Désactivation de SLA";
        };
    }
}
