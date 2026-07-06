package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ApprovalActionType;
import com.sla.monitoring.entity.enums.ApprovalTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestCreateRequest {

    @NotNull
    private ApprovalActionType actionType;

    @NotNull
    private ApprovalTargetType targetType;

    @NotNull
    private Long targetId;

    private String reason;
}
