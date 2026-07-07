package com.sla.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiProperties {

    private boolean enabled = true;
    private String apiKey = "";
    private String model = "gemini-2.5-flash";
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
}
