package com.sla.monitoring.alert;

import com.sla.monitoring.engine.model.SlaEvaluationResult;
import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.notification.AlertNotificationService;
import com.sla.monitoring.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Creates automatic alerts for SLA breaches, service outages and high error rates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutomaticAlertService {

    private static final Duration DEDUP_WINDOW = Duration.ofHours(1);

    private final AlertRepository alertRepository;
    private final AlertNotificationService alertNotificationService;

    @Transactional
    public boolean createSlaBreachedAlert(Sla sla, SlaEvaluationResult result) {
        String message = String.format(
                "SLA BREACHED : '%s' — disponibilité %.2f%% (objectif %.2f%%), erreur %.2f%% (limite %.2f%%)",
                sla.getName(),
                result.getUptimePercentage(),
                sla.getUptimeTarget(),
                result.getAverageErrorRate(),
                sla.getErrorRateLimit());
        return createIfAbsent(sla, null, AlertType.WEB, message, "SLA BREACHED");
    }

    @Transactional
    public boolean createSlaWarningAlert(Sla sla, SlaEvaluationResult result) {
        String message = String.format(
                "SLA WARNING : '%s' — score %.2f, disponibilité %.2f%%, temps réponse %.2f ms",
                sla.getName(),
                result.getSlaScore(),
                result.getUptimePercentage(),
                result.getAverageResponseTime());
        return createIfAbsent(sla, null, AlertType.WEB, message, "SLA WARNING");
    }

    @Transactional
    public boolean createServiceDownAlert(Service service, Sla sla) {
        String message = String.format(
                "Service DOWN : '%s' indisponible sur le SLA '%s'",
                service.getName(),
                sla.getName());
        return createIfAbsent(sla, service, AlertType.WEB, message, "Service DOWN");
    }

    @Transactional
    public boolean createHighErrorRateAlert(Service service, Sla sla, double errorRate) {
        String message = String.format(
                "Taux d'erreur élevé : '%s' à %.2f%% (limite SLA %.2f%%)",
                service.getName(),
                errorRate,
                sla.getErrorRateLimit());
        return createIfAbsent(sla, service, AlertType.EMAIL, message, "Taux d'erreur élevé");
    }

    @Transactional
    public boolean createAlertsFromEvaluation(Sla sla, SlaEvaluationResult result) {
        boolean created = false;

        if (result.getCurrentStatus() == SlaStatus.BREACHED) {
            created |= createSlaBreachedAlert(sla, result);
        } else if (result.isStatusChanged() && result.getCurrentStatus() == SlaStatus.WARNING) {
            created |= createSlaWarningAlert(sla, result);
        }

        return created;
    }

    private boolean createIfAbsent(Sla sla,
                                   Service service,
                                   AlertType type,
                                   String message,
                                   String dedupToken) {
        if (hasRecentDuplicate(sla.getId(), service != null ? service.getId() : null, dedupToken)) {
            log.debug("Skipping duplicate alert for SLA {}: {}", sla.getId(), dedupToken);
            return false;
        }

        Alert alert = Alert.builder()
                .type(type)
                .message(message)
                .status(AlertStatus.NEW)
                .sla(sla)
                .service(service)
                .build();

        Alert saved = alertRepository.save(alert);
        alertNotificationService.dispatch(saved.getId());
        log.info("Automatic alert created for SLA '{}': {}", sla.getName(), dedupToken);
        return true;
    }

    private boolean hasRecentDuplicate(Long slaId, Long serviceId, String dedupToken) {
        LocalDateTime since = LocalDateTime.now().minus(DEDUP_WINDOW);
        return alertRepository.findBySlaId(slaId).stream()
                .filter(alert -> alert.getStatus() == AlertStatus.NEW)
                .filter(alert -> alert.getCreatedAt() != null && alert.getCreatedAt().isAfter(since))
                .filter(alert -> alert.getMessage() != null && alert.getMessage().contains(dedupToken))
                .anyMatch(alert -> matchesService(alert, serviceId));
    }

    private boolean matchesService(Alert alert, Long serviceId) {
        if (serviceId == null) {
            return alert.getService() == null;
        }
        return alert.getService() != null && serviceId.equals(alert.getService().getId());
    }
}
