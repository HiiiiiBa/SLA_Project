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
public class IncidentCommentResponse {

    private Long id;
    private Long incidentId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}
