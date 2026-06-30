package com.sla.monitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables asynchronous alert notification dispatch.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
