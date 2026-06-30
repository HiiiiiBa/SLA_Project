package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.SlaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaWithServicesResponse {

    private Long id;
    private String name;
    private SlaStatus status;
    private Double uptimeTarget;
    private Integer responseTimeLimit;
    private Double errorRateLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ServiceEntityResponse> services;
}
