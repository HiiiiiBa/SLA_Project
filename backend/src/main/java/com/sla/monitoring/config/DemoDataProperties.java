package com.sla.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for demo dataset seeding in development.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.demo")
public class DemoDataProperties {

    private boolean seedEnabled = true;
}
