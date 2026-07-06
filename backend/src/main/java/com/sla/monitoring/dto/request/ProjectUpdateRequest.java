package com.sla.monitoring.dto.request;

import com.sla.monitoring.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private ProjectStatus status;

    @NotNull
    private Long clientId;

    private Long teamId;

    private List<Long> memberIds;
}
