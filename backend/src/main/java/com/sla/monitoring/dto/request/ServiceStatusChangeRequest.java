package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatusChangeRequest {

    @NotNull(message = "Status is required")
    private ServiceStatus status;
}
