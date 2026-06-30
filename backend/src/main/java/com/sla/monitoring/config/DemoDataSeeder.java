package com.sla.monitoring.config;

import com.sla.monitoring.entity.Client;
import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.entity.enums.ServiceStatus;
import com.sla.monitoring.entity.enums.SlaStatus;
import com.sla.monitoring.repository.ClientRepository;
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

/**
 * Seeds a demo client, SLA, services and sample users for local development.
 */
@Slf4j
@Component
@Profile({"dev", "docker"})
@Order(2)
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_CLIENT_EMAIL = "client@acme.com";

    private final DemoDataProperties demoDataProperties;
    private final ClientRepository clientRepository;
    private final SlaRepository slaRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoDataProperties.isSeedEnabled()) {
            log.info("Demo data seeding disabled");
            return;
        }

        if (clientRepository.findByEmail(DEMO_CLIENT_EMAIL).isPresent()) {
            log.info("Demo data already present, skipping seed");
            return;
        }

        Client client = clientRepository.save(Client.builder()
                .name("Acme Corp")
                .email(DEMO_CLIENT_EMAIL)
                .projectName("Production Platform")
                .build());

        Sla sla = slaRepository.save(Sla.builder()
                .name("Production API SLA")
                .status(SlaStatus.ACTIVE)
                .uptimeTarget(99.9)
                .responseTimeLimit(500)
                .errorRateLimit(1.0)
                .client(client)
                .build());

        createService("API Gateway", sla);
        createService("Auth Service", sla);
        createService("Database Cluster", sla);

        createUserIfAbsent("Demo", "User", "user@sla.com", "User123!", Role.USER);
        createUserIfAbsent("Acme", "Client", DEMO_CLIENT_EMAIL, "Client123!", Role.CLIENT);

        log.info("Demo data seeded: client '{}', SLA '{}', 3 services, 2 users",
                client.getName(), sla.getName());
    }

    private void createService(String name, Sla sla) {
        serviceRepository.save(Service.builder()
                .name(name)
                .status(ServiceStatus.UP)
                .sla(sla)
                .build());
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
        log.info("Demo user created: {} / {}", email, password);
    }
}
