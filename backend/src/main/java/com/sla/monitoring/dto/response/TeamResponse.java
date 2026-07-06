package com.sla.monitoring.dto.response;

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
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private List<TeamMemberResponse> members;
    private int memberCount;
    private int projectCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
