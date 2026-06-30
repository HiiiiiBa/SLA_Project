package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ServiceStatus;
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
public class ServiceEntityUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Status is required")
    private ServiceStatus status;

    private Long slaId;
}
