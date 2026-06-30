package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Service;
import com.sla.monitoring.entity.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findBySlaId(Long slaId);

    List<Service> findByStatus(ServiceStatus status);
}
