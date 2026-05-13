package com.main_project.notification_service.repository;

import com.main_project.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);

    long countByUserIdAndStatus(UUID userId, String status);

    List<Notification> findByTemplateIdOrderByCreatedAtDesc(String templateId);
}