package com.sla.monitoring.notification;

import com.sla.monitoring.config.AlertNotificationProperties;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.notification.dto.AlertNotificationMessage;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.UserRepository;
import com.sla.monitoring.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlertNotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AlertNotificationProperties properties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlertNotificationServiceImpl alertNotificationService;

    private Alert alert;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .id(1L)
                .name("Acme Corp")
                .email("client@acme.com")
                .build();
        Sla sla = Sla.builder()
                .id(2L)
                .name("Production SLA")
                .client(client)
                .build();
        alert = Alert.builder()
                .id(10L)
                .type(AlertType.EMAIL)
                .status(AlertStatus.NEW)
                .message("SLA breached")
                .sla(sla)
                .build();
        alert.setCreatedAt(LocalDateTime.now());

        when(alertRepository.findByIdWithSlaAndClient(10L)).thenReturn(Optional.of(alert));
        when(properties.isWebsocketEnabled()).thenReturn(true);
        when(properties.isEmailEnabled()).thenReturn(true);
        when(properties.isEmailAlsoForWebAlerts()).thenReturn(true);
        when(properties.isNotifyClient()).thenReturn(true);
        when(properties.isNotifyAdmins()).thenReturn(true);
        when(properties.getFromAddress()).thenReturn("alerts@sla.com");
        when(properties.getFromName()).thenReturn("SLA Monitoring");
        when(userRepository.findByRoleAndEnabledTrue(Role.ADMIN))
                .thenReturn(List.of(User.builder().email("admin@sla.com").build()));
        when(mailSender.createMimeMessage()).thenReturn(new jakarta.mail.internet.MimeMessage(
                (jakarta.mail.Session) null));
    }

    @Test
    @DisplayName("Dispatches WebSocket notification on alert creation")
    void dispatchPublishesWebSocketMessage() {
        alertNotificationService.dispatch(10L);

        ArgumentCaptor<AlertNotificationMessage> captor = ArgumentCaptor.forClass(AlertNotificationMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq(AlertNotificationServiceImpl.ALERTS_TOPIC),
                captor.capture());

        AlertNotificationMessage message = captor.getValue();
        assertThat(message.getAlertId()).isEqualTo(10L);
        assertThat(message.getSlaName()).isEqualTo("Production SLA");
        assertThat(message.getClientName()).isEqualTo("Acme Corp");
    }

    @Test
    @DisplayName("Sends email to client and admins for EMAIL alerts")
    void dispatchSendsEmailForEmailAlerts() {
        alertNotificationService.dispatch(10L);

        verify(mailSender).send(org.mockito.ArgumentMatchers.any(jakarta.mail.internet.MimeMessage.class));
    }
}
