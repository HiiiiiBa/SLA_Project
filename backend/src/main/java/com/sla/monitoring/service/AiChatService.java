package com.sla.monitoring.service;

import com.sla.monitoring.dto.request.AiChatRequest;
import com.sla.monitoring.dto.response.AiChatResponse;

public interface AiChatService {

    AiChatResponse chat(AiChatRequest request);
}
