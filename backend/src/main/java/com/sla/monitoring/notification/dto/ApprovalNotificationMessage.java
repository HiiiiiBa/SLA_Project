package com.sla.monitoring.notification.dto;

import com.sla.monitoring.entity.enums.ApprovalActionType;
import com.sla.monitoring.entity.enums.ApprovalRequestStatus;
import com.sla.monitoring.entity.enums.ApprovalTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalNotificationMessage {

    public static final String KIND_SUBMITTED = "SUBMITTED";
    public static final String KIND_APPROVED = "APPROVED";
    public static final String KIND_REJECTED = "REJECTED";

    private Long requestId;
    private String kind;
    private ApprovalActionType actionType;
    private ApprovalTargetType targetType;
    private Long targetId;
    private String targetLabel;
    private String message;
    private ApprovalRequestStatus status;
    private String requesterName;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime createdAt;
}
