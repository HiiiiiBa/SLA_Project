package com.sla.monitoring.repository;

import com.sla.monitoring.entity.Notification;
import com.sla.monitoring.entity.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByChannelOrderByCreatedAtDesc(NotificationChannel channel);

    List<Notification> findAllByOrderByCreatedAtDesc();
}
