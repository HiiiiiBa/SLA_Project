package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findBySlaId(Long slaId);

    List<Service> findByStatus(ServiceStatus status);

    @Query("SELECT s FROM Service s JOIN FETCH s.sla WHERE s.sla.id = :slaId ORDER BY s.name ASC")
    List<Service> findBySlaIdWithSla(@Param("slaId") Long slaId);

    @Query("SELECT s FROM Service s JOIN FETCH s.sla ORDER BY s.name ASC")
    List<Service> findAllWithSla();

    @Query("SELECT s FROM Service s JOIN FETCH s.sla WHERE s.sla.id IN :slaIds ORDER BY s.name ASC")
    List<Service> findBySlaIdInWithSla(@Param("slaIds") Collection<Long> slaIds);
}
