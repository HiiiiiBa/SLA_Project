package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Sla;
import com.sla.monitoring.entity.enums.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaRepository extends JpaRepository<Sla, Long> {

    List<Sla> findByClientId(Long clientId);

    List<Sla> findByStatus(SlaStatus status);

    List<Sla> findByStatusNot(SlaStatus status);
}
