package com.sla.monitoring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String name;
    private String email;
    private String projectName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
