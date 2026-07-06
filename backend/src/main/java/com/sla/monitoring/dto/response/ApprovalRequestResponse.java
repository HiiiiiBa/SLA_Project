package com.sla.monitoring.dto.response;

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
public class ApprovalRequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private ApprovalActionType actionType;
    private ApprovalTargetType targetType;
    private Long targetId;
    private String targetLabel;
    private String reason;
    private ApprovalRequestStatus status;
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
}
