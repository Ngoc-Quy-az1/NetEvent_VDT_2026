package com.example.notification.adapter.infrastructure;

import com.example.notification.adapter.domain.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findByNotificationEventId(UUID notificationEventId);
    List<EventEntity> findTop500ByStatusOrderByReceivedAtAsc(String status);
    List<EventEntity> findByBatchId(String batchId);
}
