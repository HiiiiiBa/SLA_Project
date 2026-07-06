package com.sla.monitoring.dto.response;

import com.sla.monitoring.entity.enums.ProjectStatus;
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
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Long clientId;
    private String clientName;
    private Long teamId;
    private String teamName;
    private Long slaId;
    private String slaName;
    private String managerName;
    private List<TeamMemberResponse> assignedMembers;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
