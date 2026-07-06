package com.sla.monitoring.repository;

import com.sla.monitoring.entity.User;
import com.sla.monitoring.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleAndEnabledTrue(Role role);

    List<User> findByRole(Role role);
}
