package com.example.notification.processor.repository;
import com.example.notification.processor.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByNotificationEventId(UUID notificationEventId);
    List<Notification> findByStatusIn(Collection<String> statuses);
}
