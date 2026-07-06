package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDraftRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    @Builder.Default
    private ServiceStatus status = ServiceStatus.UP;
}
