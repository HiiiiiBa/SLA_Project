package com.sla.monitoring.notification;

import com.sla.monitoring.entity.Project;
import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import com.sla.monitoring.repository.ClientRepository;
import com.sla.monitoring.repository.ProjectRepository;
import com.sla.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves which user accounts should receive real-time alert notifications for a given SLA/client.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertRecipientResolver {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    public Set<String> resolveRecipientEmails(Long clientId, Long slaId) {
        Set<String> emails = new LinkedHashSet<>();

        userRepository.findByRoleAndEnabledTrue(Role.ADMIN).stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .forEach(emails::add);

        clientRepository.findByIdWithManagers(clientId).ifPresent(client -> {
            client.getManagers().stream()
                    .filter(User::isEnabled)
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .forEach(emails::add);

            userRepository.findByEmail(client.getEmail())
                    .filter(user -> user.getRole() == Role.CLIENT && user.isEnabled())
                    .map(User::getEmail)
                    .ifPresent(emails::add);
        });

        for (Project project : projectRepository.findBySlaIdWithMembers(slaId)) {
            project.getAssignedMembers().stream()
                    .filter(User::isEnabled)
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .forEach(emails::add);
        }

        return emails;
    }
}
