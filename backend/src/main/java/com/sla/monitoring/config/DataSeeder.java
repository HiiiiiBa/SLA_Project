package com.sla.monitoring.config;

import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds or synchronizes the default admin account in development.
 */
@Slf4j
@Component
@Profile({"dev", "docker", "test"})
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@sla.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
                this::synchronizeAdmin,
                this::createAdmin
        );
    }

    private void synchronizeAdmin(User admin) {
        admin.setFirstName("Admin");
        admin.setLastName("System");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        userRepository.save(admin);
        log.info("Admin user synchronized: {}", ADMIN_EMAIL);
    }

    private void createAdmin() {
        User admin = User.builder()
                .firstName("Admin")
                .lastName("System")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", ADMIN_EMAIL);
    }
}
