package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntityResponse {

    private Long id;
    private String name;
    private ServiceStatus status;
    private Long slaId;
    private String slaName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
