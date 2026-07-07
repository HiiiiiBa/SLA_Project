package com.sla.monitoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageRequest {

    @NotBlank
    private String role;

    @NotBlank
    @Size(max = 4000)
    private String content;
}
