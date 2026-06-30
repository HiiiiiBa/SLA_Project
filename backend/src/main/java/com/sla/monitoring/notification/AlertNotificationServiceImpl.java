package com.sla.monitoring.notification;

import com.sla.monitoring.config.AlertNotificationProperties;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.notification.dto.AlertNotificationMessage;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Sends alert notifications via email and WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationServiceImpl implements AlertNotificationService {

    public static final String ALERTS_TOPIC = "/topic/alerts";

    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate messagingTemplate;
    private final AlertNotificationProperties properties;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;

    @Override
    @Async
    public void dispatch(Long alertId) {
        Alert alert = alertRepository.findByIdWithSlaAndClient(alertId).orElse(null);
        if (alert == null) {
            log.warn("Alert not found for notification dispatch: id={}", alertId);
            return;
        }

        Sla sla = alert.getSla();
        Client client = sla.getClient();
        AlertNotificationMessage message = toMessage(alert, sla, client);

        if (properties.isWebsocketEnabled()) {
            publishWebSocket(message);
        }

        if (shouldSendEmail(alert.getType())) {
            sendEmail(alert, sla, client);
        }
    }

    private void publishWebSocket(AlertNotificationMessage message) {
        messagingTemplate.convertAndSend(ALERTS_TOPIC, message);
        log.info("WebSocket alert published: alertId={}, slaId={}", message.getAlertId(), message.getSlaId());
    }

    private void sendEmail(Alert alert, Sla sla, Client client) {
        Set<String> recipients = resolveRecipients(client);
        if (recipients.isEmpty()) {
            log.warn("No email recipients configured for alert id={}", alert.getId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getFromAddress(), properties.getFromName());
            helper.setTo(recipients.toArray(String[]::new));
            helper.setSubject(buildSubject(sla));
            helper.setText(buildEmailBody(alert, sla, client), false);
            mailSender.send(mimeMessage);
            log.info("Alert email sent to {} recipient(s) for alert id={}", recipients.size(), alert.getId());
        } catch (MessagingException ex) {
            log.error("Failed to send alert email for alert id={}", alert.getId(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error while sending alert email for alert id={}", alert.getId(), ex);
        }
    }

    private Set<String> resolveRecipients(Client client) {
        Set<String> recipients = new LinkedHashSet<>();

        if (properties.isNotifyClient() && client.getEmail() != null && !client.getEmail().isBlank()) {
            recipients.add(client.getEmail());
        }

        if (properties.isNotifyAdmins()) {
            userRepository.findByRoleAndEnabledTrue(Role.ADMIN).stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .forEach(recipients::add);
        }

        return recipients;
    }

    private boolean shouldSendEmail(AlertType type) {
        if (!properties.isEmailEnabled()) {
            return false;
        }
        return type == AlertType.EMAIL
                || (type == AlertType.WEB && properties.isEmailAlsoForWebAlerts());
    }

    private AlertNotificationMessage toMessage(Alert alert, Sla sla, Client client) {
        return AlertNotificationMessage.builder()
                .alertId(alert.getId())
                .type(alert.getType())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .slaId(sla.getId())
                .slaName(sla.getName())
                .clientName(client.getName())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private String buildSubject(Sla sla) {
        return "[SLA Alert] " + sla.getName();
    }

    private String buildEmailBody(Alert alert, Sla sla, Client client) {
        return """
                SLA Monitoring Alert

                Client: %s
                SLA: %s
                Status: %s
                Type: %s

                Message:
                %s

                Generated at: %s
                """.formatted(
                client.getName(),
                sla.getName(),
                alert.getStatus(),
                alert.getType(),
                alert.getMessage(),
                alert.getCreatedAt()
        );
    }
}
