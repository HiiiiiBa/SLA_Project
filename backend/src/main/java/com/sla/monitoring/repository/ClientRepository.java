package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    @Query("""
            SELECT DISTINCT c FROM Client c
            JOIN c.managers m
            WHERE m.id = :managerId
            ORDER BY c.name
            """)
    List<Client> findByManagerId(@Param("managerId") Long managerId);

    @Query("""
            SELECT c.id FROM Client c
            JOIN c.managers m
            WHERE m.id = :managerId
            """)
    List<Long> findClientIdsByManagerId(@Param("managerId") Long managerId);

    @Query("""
            SELECT DISTINCT c FROM Client c
            LEFT JOIN FETCH c.managers
            WHERE c.id = :id
            """)
    Optional<Client> findByIdWithManagers(@Param("id") Long id);
}
