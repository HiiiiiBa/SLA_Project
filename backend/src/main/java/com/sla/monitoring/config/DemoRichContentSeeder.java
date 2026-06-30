package com.sla.monitoring.config;

import com.sla.monitoring.entity.Alert;
import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Incident;
import com.sla.monitoring.entity.MonitoringMetric;
import com.sla.monitoring.entity.Notification;
import com.sla.monitoring.entity.Report;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.AlertStatus;
import com.sla.monitoring.entity.enums.AlertType;
import com.sla.monitoring.entity.enums.IncidentSeverity;
import com.sla.monitoring.entity.enums.MetricStatus;
import com.sla.monitoring.entity.enums.NotificationChannel;
import com.sla.monitoring.entity.enums.NotificationStatus;
import com.sla.monitoring.entity.enums.ReportFormat;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.repository.AlertRepository;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.IncidentRepository;
import com.sla.monitoring.repository.MonitoringMetricRepository;
import com.sla.monitoring.repository.NotificationRepository;
import com.sla.monitoring.repository.ReportRepository;
import com.sla.monitoring.repository.ServiceRepository;
import com.sla.monitoring.repository.SlaRepository;
import com.sla.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seeds rich demo content (metrics history, incidents, alerts, reports)
 * so the UI is populated on first launch or when metrics are empty.
 */
@Slf4j
@Component
@Profile({"dev", "docker"})
@Order(3)
@RequiredArgsConstructor
public class DemoRichContentSeeder implements CommandLineRunner {

    private final DemoDataProperties demoDataProperties;
    private final ClientRepository clientRepository;
    private final SlaRepository slaRepository;
    private final ServiceRepository serviceRepository;
    private final MonitoringMetricRepository metricRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoDataProperties.isSeedEnabled()) {
            return;
        }

        boolean needsCatalog = slaRepository.count() < 6;
        boolean needsMetrics = metricRepository.count() == 0;
        boolean needsIncidents = incidentRepository.count() == 0;
        boolean needsAlerts = alertRepository.count() < 3;
        boolean needsReports = reportRepository.count() == 0;
        boolean needsNotifications = notificationRepository.count() == 0;

        if (!needsCatalog && !needsMetrics && !needsIncidents && !needsAlerts
                && !needsReports && !needsNotifications) {
            log.info("Rich demo content already complete, skipping seed");
            return;
        }

        log.info("Seeding rich demo content (catalog={}, metrics={}, incidents={}, alerts={}, reports={})",
                needsCatalog, needsMetrics, needsIncidents, needsAlerts, needsReports);

        Client acme = upsertClient("Acme Corp", "client@acme.com", "Plateforme de production");
        Client techStart = upsertClient("TechStart SA", "client@techstart.fr", "SaaS B2B");
        Client retail = upsertClient("Global Retail Ltd", "client@globalretail.com", "E-commerce Europe");
        Client finserv = upsertClient("FinServ Partners", "client@finserv.com", "API bancaire");

        Sla acmeProd = upsertSla(acme, "Production API SLA", SlaStatus.ACTIVE, 99.9, 500, 1.0);
        Sla acmePayment = upsertSla(acme, "Payment Gateway SLA", SlaStatus.WARNING, 99.95, 300, 0.5);
        Sla techWeb = upsertSla(techStart, "Application Web SLA", SlaStatus.ACTIVE, 99.5, 800, 2.0);
        Sla techApi = upsertSla(techStart, "Core API SLA", SlaStatus.BREACHED, 99.9, 400, 0.8);
        Sla retailShop = upsertSla(retail, "Boutique en ligne SLA", SlaStatus.ACTIVE, 99.0, 600, 1.5);
        Sla finservCore = upsertSla(finserv, "Core Banking SLA", SlaStatus.ARCHIVED, 99.99, 200, 0.1);

        List<Service> acmeProdServices = ensureServices(acmeProd, List.of(
                serviceDef("API Gateway", ServiceStatus.UP),
                serviceDef("Auth Service", ServiceStatus.UP),
                serviceDef("Database Cluster", ServiceStatus.UP)
        ));
        List<Service> acmePaymentServices = ensureServices(acmePayment, List.of(
                serviceDef("Payment API", ServiceStatus.UP),
                serviceDef("Fraud Detection", ServiceStatus.DOWN),
                serviceDef("Settlement Worker", ServiceStatus.UP)
        ));
        List<Service> techWebServices = ensureServices(techWeb, List.of(
                serviceDef("Frontend CDN", ServiceStatus.UP),
                serviceDef("Session Store", ServiceStatus.UP)
        ));
        List<Service> techApiServices = ensureServices(techApi, List.of(
                serviceDef("REST API", ServiceStatus.DOWN),
                serviceDef("Worker Queue", ServiceStatus.UP),
                serviceDef("Cache Redis", ServiceStatus.DOWN)
        ));
        List<Service> retailServices = ensureServices(retailShop, List.of(
                serviceDef("Catalogue API", ServiceStatus.UP),
                serviceDef("Checkout Service", ServiceStatus.UP),
                serviceDef("Search Engine", ServiceStatus.UP)
        ));
        List<Service> finservServices = ensureServices(finservCore, List.of(
                serviceDef("Transaction API", ServiceStatus.UP),
                serviceDef("Ledger Service", ServiceStatus.UP)
        ));

        createUserIfAbsent("Demo", "User", "user@sla.com", "User123!", Role.USER);
        createUserIfAbsent("Acme", "Client", "client@acme.com", "Client123!", Role.CLIENT);
        createUserIfAbsent("Marie", "Dupont", "client@techstart.fr", "Client123!", Role.CLIENT);
        createUserIfAbsent("James", "Wilson", "client@globalretail.com", "Client123!", Role.CLIENT);

        if (needsMetrics) {
            seedMetrics(acmeProdServices, acmeProd, MetricProfile.HEALTHY);
            seedMetrics(acmePaymentServices, acmePayment, MetricProfile.WARNING);
            seedMetrics(techWebServices, techWeb, MetricProfile.HEALTHY);
            seedMetrics(techApiServices, techApi, MetricProfile.BREACHED);
            seedMetrics(retailServices, retailShop, MetricProfile.HEALTHY);
            seedMetrics(finservServices, finservCore, MetricProfile.STABLE);
        }

        if (needsIncidents) {
            seedIncidents(acmeProd, acmePayment, techApi, retailShop);
        }
        if (needsAlerts) {
            seedAlerts(acmePayment, techApi, acmeProd);
        }
        if (needsReports) {
            seedReports(acmeProd, acmePayment, techWeb, techApi, retailShop);
        }
        if (needsNotifications) {
            seedNotifications(acmePayment, techApi, acmeProd);
        }

        log.info("Rich demo content seeded: {} clients, {} SLAs, {} metrics, {} incidents, {} alerts, {} reports, {} notifications",
                clientRepository.count(),
                slaRepository.count(),
                metricRepository.count(),
                incidentRepository.count(),
                alertRepository.count(),
                reportRepository.count(),
                notificationRepository.count());
    }

    private Client upsertClient(String name, String email, String projectName) {
        return clientRepository.findByEmail(email).orElseGet(() ->
                clientRepository.save(Client.builder()
                        .name(name)
                        .email(email)
                        .projectName(projectName)
                        .build()));
    }

    private Sla upsertSla(Client client, String name, SlaStatus status,
                            double uptimeTarget, int responseLimit, double errorLimit) {
        return slaRepository.findByClientId(client.getId()).stream()
                .filter(sla -> sla.getName().equals(name))
                .findFirst()
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setUptimeTarget(uptimeTarget);
                    existing.setResponseTimeLimit(responseLimit);
                    existing.setErrorRateLimit(errorLimit);
                    return slaRepository.save(existing);
                })
                .orElseGet(() -> slaRepository.save(Sla.builder()
                        .name(name)
                        .status(status)
                        .uptimeTarget(uptimeTarget)
                        .responseTimeLimit(responseLimit)
                        .errorRateLimit(errorLimit)
                        .client(client)
                        .build()));
    }

    private record ServiceDef(String name, ServiceStatus status) {}

    private ServiceDef serviceDef(String name, ServiceStatus status) {
        return new ServiceDef(name, status);
    }

    private List<Service> ensureServices(Sla sla, List<ServiceDef> definitions) {
        List<Service> existing = serviceRepository.findBySlaId(sla.getId());
        List<Service> result = new ArrayList<>();

        for (ServiceDef def : definitions) {
            Service service = existing.stream()
                    .filter(item -> item.getName().equals(def.name()))
                    .findFirst()
                    .orElseGet(() -> serviceRepository.save(Service.builder()
                            .name(def.name())
                            .status(def.status())
                            .sla(sla)
                            .build()));
            service.setStatus(def.status());
            result.add(serviceRepository.save(service));
        }
        return result;
    }

    private void seedMetrics(List<Service> services, Sla sla, MetricProfile profile) {
        LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime start = end.minusDays(7);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<MonitoringMetric> batch = new ArrayList<>();

        for (LocalDateTime ts = start; !ts.isAfter(end); ts = ts.plusHours(1)) {
            for (Service service : services) {
                boolean isUp = random.nextDouble() >= profile.downProbability(service.getStatus());
                double responseTime = profile.responseTime(sla, isUp, random);
                double errorRate = profile.errorRate(sla, isUp, random);

                batch.add(MonitoringMetric.builder()
                        .timestamp(ts)
                        .responseTime(round(responseTime))
                        .status(isUp ? MetricStatus.UP : MetricStatus.DOWN)
                        .errorRate(round(errorRate))
                        .service(service)
                        .sla(sla)
                        .build());
            }
        }
        metricRepository.saveAll(batch);
    }

    private void seedIncidents(Sla acmeProd, Sla acmePayment, Sla techApi, Sla retailShop) {
        LocalDateTime now = LocalDateTime.now();

        incidentRepository.saveAll(List.of(
                Incident.builder()
                        .sla(acmeProd)
                        .severity(IncidentSeverity.LOW)
                        .description("Pic de latence sur l'API Gateway lors du lancement marketing (+40 % de trafic).")
                        .startTime(now.minusDays(5).minusHours(3))
                        .endTime(now.minusDays(5).minusHours(1))
                        .build(),
                Incident.builder()
                        .sla(acmePayment)
                        .severity(IncidentSeverity.HIGH)
                        .description("Dégradation du service Fraud Detection — taux d'erreur au-dessus du seuil SLA.")
                        .startTime(now.minusDays(2).minusHours(6))
                        .endTime(null)
                        .build(),
                Incident.builder()
                        .sla(techApi)
                        .severity(IncidentSeverity.CRITICAL)
                        .description("Panne majeure sur REST API : indisponibilité totale pendant 2 h 15.")
                        .startTime(now.minusDays(1).minusHours(8))
                        .endTime(now.minusDays(1).minusHours(6))
                        .build(),
                Incident.builder()
                        .sla(techApi)
                        .severity(IncidentSeverity.MEDIUM)
                        .description("Saturation du cache Redis — temps de réponse multiplié par 4.")
                        .startTime(now.minusHours(12))
                        .endTime(now.minusHours(9))
                        .build(),
                Incident.builder()
                        .sla(retailShop)
                        .severity(IncidentSeverity.LOW)
                        .description("Ralentissement du moteur de recherche lors du Black Friday preview.")
                        .startTime(now.minusDays(3))
                        .endTime(now.minusDays(3).plusHours(2))
                        .build(),
                Incident.builder()
                        .sla(acmeProd)
                        .severity(IncidentSeverity.MEDIUM)
                        .description("Maintenance corrective sur le cluster base de données — failover réussi.")
                        .startTime(now.minusDays(10))
                        .endTime(now.minusDays(10).plusHours(1))
                        .build()
        ));
    }

    private void seedAlerts(Sla acmePayment, Sla techApi, Sla acmeProd) {
        LocalDateTime now = LocalDateTime.now();

        alertRepository.saveAll(List.of(
                Alert.builder()
                        .sla(acmePayment)
                        .type(AlertType.WEB)
                        .status(AlertStatus.NEW)
                        .message("SLA Payment Gateway en WARNING : temps de réponse moyen à 92 % de la limite (276 ms / 300 ms).")
                        .build(),
                Alert.builder()
                        .sla(techApi)
                        .type(AlertType.EMAIL)
                        .status(AlertStatus.NEW)
                        .message("SLA Core API BREACHED : disponibilité à 78 % sur les dernières 24 h (objectif 99.9 %).")
                        .build(),
                Alert.builder()
                        .sla(techApi)
                        .type(AlertType.WEB)
                        .status(AlertStatus.READ)
                        .message("Taux d'erreur élevé détecté sur REST API (2.1 % — limite 0.8 %).")
                        .build(),
                Alert.builder()
                        .sla(acmePayment)
                        .type(AlertType.EMAIL)
                        .status(AlertStatus.RESOLVED)
                        .message("Alerte résolue : Fraud Detection rétabli après redémarrage du pod.")
                        .build(),
                Alert.builder()
                        .sla(acmeProd)
                        .type(AlertType.WEB)
                        .status(AlertStatus.READ)
                        .message("Pic de latence transitoire sur API Gateway — retour à la normale.")
                        .build()
        ));
    }

    private void seedReports(Sla... slas) {
        LocalDateTime now = LocalDateTime.now();
        List<Report> reports = new ArrayList<>();

        for (Sla sla : slas) {
            for (int month = 2; month >= 0; month--) {
                LocalDateTime periodEnd = now.minusMonths(month).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                LocalDateTime periodStart = periodEnd.minusMonths(1);
                double slaResult = switch (sla.getStatus()) {
                    case BREACHED -> 72.5 + month * 2;
                    case WARNING -> 88.0 + month;
                    case ARCHIVED -> 99.2;
                    default -> 96.5 + month * 0.8;
                };

                reports.add(Report.builder()
                        .sla(sla)
                        .slaResult(round(slaResult))
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .generatedAt(periodEnd.plusDays(2))
                        .format(month % 2 == 0 ? ReportFormat.PDF : ReportFormat.CSV)
                        .build());
            }
        }
        reportRepository.saveAll(reports);
    }

    private void seedNotifications(Sla acmePayment, Sla techApi, Sla acmeProd) {
        LocalDateTime now = LocalDateTime.now();

        notificationRepository.saveAll(List.of(
                Notification.builder()
                        .channel(NotificationChannel.WEBSOCKET)
                        .status(NotificationStatus.SENT)
                        .recipient("broadcast")
                        .message("SLA Payment Gateway en WARNING : temps de réponse moyen à 92 % de la limite.")
                        .slaId(acmePayment.getId())
                        .slaName(acmePayment.getName())
                        .clientName(acmePayment.getClient().getName())
                        .createdAt(now.minusHours(6))
                        .build(),
                Notification.builder()
                        .channel(NotificationChannel.EMAIL)
                        .status(NotificationStatus.SENT)
                        .recipient("client@techstart.fr, admin@sla.com")
                        .message("SLA Core API BREACHED : disponibilité à 78 % sur les dernières 24 h.")
                        .slaId(techApi.getId())
                        .slaName(techApi.getName())
                        .clientName(techApi.getClient().getName())
                        .createdAt(now.minusHours(5))
                        .build(),
                Notification.builder()
                        .channel(NotificationChannel.WEBSOCKET)
                        .status(NotificationStatus.SENT)
                        .recipient("broadcast")
                        .message("Taux d'erreur élevé détecté sur REST API (2.1 % — limite 0.8 %).")
                        .slaId(techApi.getId())
                        .slaName(techApi.getName())
                        .clientName(techApi.getClient().getName())
                        .createdAt(now.minusHours(3))
                        .build(),
                Notification.builder()
                        .channel(NotificationChannel.EMAIL)
                        .status(NotificationStatus.SENT)
                        .recipient("client@acme.com, admin@sla.com")
                        .message("Alerte résolue : Fraud Detection rétabli après redémarrage du pod.")
                        .slaId(acmePayment.getId())
                        .slaName(acmePayment.getName())
                        .clientName(acmePayment.getClient().getName())
                        .createdAt(now.minusDays(1))
                        .build(),
                Notification.builder()
                        .channel(NotificationChannel.EMAIL)
                        .status(NotificationStatus.FAILED)
                        .recipient("client@acme.com")
                        .message("Pic de latence transitoire sur API Gateway — envoi email échoué (SMTP indisponible).")
                        .slaId(acmeProd.getId())
                        .slaName(acmeProd.getName())
                        .clientName(acmeProd.getClient().getName())
                        .createdAt(now.minusDays(2))
                        .build()
        ));
    }

    private void createUserIfAbsent(String firstName, String lastName, String email,
                                      String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build());
        log.info("Demo user created: {}", email);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private enum MetricProfile {
        HEALTHY(0.02, 0.25, 0.65, 0.45),
        WARNING(0.12, 0.55, 0.92, 0.85),
        BREACHED(0.30, 0.75, 1.25, 1.10),
        STABLE(0.01, 0.15, 0.40, 0.30);

        private final double downChance;
        private final double responseMinRatio;
        private final double responseMaxRatio;
        private final double errorMaxRatio;

        MetricProfile(double downChance, double responseMinRatio, double responseMaxRatio, double errorMaxRatio) {
            this.downChance = downChance;
            this.responseMinRatio = responseMinRatio;
            this.responseMaxRatio = responseMaxRatio;
            this.errorMaxRatio = errorMaxRatio;
        }

        double downProbability(ServiceStatus serviceStatus) {
            return serviceStatus == ServiceStatus.DOWN ? Math.min(0.85, downChance + 0.35) : downChance;
        }

        double responseTime(Sla sla, boolean isUp, ThreadLocalRandom random) {
            int limit = sla.getResponseTimeLimit();
            if (!isUp) {
                return limit + random.nextDouble(50, limit * 0.6);
            }
            return random.nextDouble(limit * responseMinRatio, limit * responseMaxRatio);
        }

        double errorRate(Sla sla, boolean isUp, ThreadLocalRandom random) {
            double limit = sla.getErrorRateLimit();
            if (!isUp) {
                return random.nextDouble(Math.max(limit, 1.0), Math.max(limit * 2.5, 2.0));
            }
            return random.nextDouble(0.0, Math.max(limit * errorMaxRatio, 0.05));
        }
    }
}
