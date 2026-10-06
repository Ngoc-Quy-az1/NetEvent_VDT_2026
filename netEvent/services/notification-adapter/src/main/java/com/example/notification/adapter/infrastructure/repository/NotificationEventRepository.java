package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.domain.NotificationEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEventEntity, UUID> {
    Optional<NotificationEventEntity> findByNotificationEventId(UUID notificationEventId);
    List<NotificationEventEntity> findTop500ByStatusOrderByReceivedAtAsc(String status);
    List<NotificationEventEntity> findByBatchId(String batchId);
    Optional<NotificationEventEntity> findByEventId(UUID eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<NotificationEventEntity> findByStatusOrderByReceivedAtAsc(String status, Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationEventEntity e SET e.status = 'PENDING', e.processedAt = NULL " +
           "WHERE e.status = 'PROCESSING' AND e.processedAt < :cutoff")
    int requeueExpiredProcessingEvents(@Param("cutoff") Instant cutoff);
}
