package com.sla.monitoring.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Uptime target is required")
    @DecimalMin(value = "90.0", message = "Uptime target must be between 90 and 100")
    @DecimalMax(value = "100.0", message = "Uptime target must be between 90 and 100")
    private Double uptimeTarget;

    @NotNull(message = "Response time limit is required")
    @Positive(message = "Response time limit must be greater than 0")
    private Integer responseTimeLimit;

    @NotNull(message = "Error rate limit is required")
    @PositiveOrZero(message = "Error rate limit must be greater than or equal to 0")
    private Double errorRateLimit;
}
