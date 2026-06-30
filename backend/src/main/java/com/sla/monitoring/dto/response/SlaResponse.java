package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.SlaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaResponse {

    private Long id;
    private String name;
    private SlaStatus status;
    private Double uptimeTarget;
    private Integer responseTimeLimit;
    private Double errorRateLimit;
    private Long clientId;
    private String clientName;
    private Integer serviceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
