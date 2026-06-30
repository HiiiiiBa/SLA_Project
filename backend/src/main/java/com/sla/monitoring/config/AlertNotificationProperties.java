package com.sla.monitoring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for alert email and WebSocket notifications.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "alert.notification")
public class AlertNotificationProperties {

    private boolean emailEnabled = true;
    private boolean websocketEnabled = true;
    private boolean emailAlsoForWebAlerts = true;
    private boolean notifyClient = true;
    private boolean notifyAdmins = true;
    private String fromAddress = "sla-monitoring@sla.com";
    private String fromName = "SLA Monitoring";
}
