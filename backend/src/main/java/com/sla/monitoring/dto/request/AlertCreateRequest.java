package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertCreateRequest {

    @NotNull(message = "Alert type is required")
    private AlertType type;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "SLA id is required")
    private Long slaId;
}
