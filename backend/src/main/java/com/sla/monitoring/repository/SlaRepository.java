package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SlaRepository extends JpaRepository<Sla, Long> {

    List<Sla> findByClientId(Long clientId);

    List<Sla> findByStatus(SlaStatus status);

    List<Sla> findByStatusNot(SlaStatus status);

    @Query("SELECT s FROM Sla s JOIN FETCH s.client ORDER BY s.name ASC")
    List<Sla> findAllWithClient();

    @Query("SELECT s FROM Sla s JOIN FETCH s.client WHERE s.client.id = :clientId ORDER BY s.name ASC")
    List<Sla> findByClientIdWithClient(@Param("clientId") Long clientId);

    @Query("SELECT s FROM Sla s JOIN FETCH s.client WHERE s.client.id IN :clientIds ORDER BY s.name ASC")
    List<Sla> findByClientIdIn(@Param("clientIds") Set<Long> clientIds);

    @Query("SELECT s FROM Sla s JOIN FETCH s.client WHERE s.id IN :slaIds ORDER BY s.name ASC")
    List<Sla> findByIdInWithClient(@Param("slaIds") Set<Long> slaIds);

    @Query("SELECT s FROM Sla s JOIN FETCH s.client WHERE s.id = :id")
    Optional<Sla> findByIdWithClient(@Param("id") Long id);
}
