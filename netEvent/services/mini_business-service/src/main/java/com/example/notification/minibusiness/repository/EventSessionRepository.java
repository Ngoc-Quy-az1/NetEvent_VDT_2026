package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.EventSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventSessionRepository extends JpaRepository<EventSessionEntity, UUID> {
    List<EventSessionEntity> findByEventId(UUID eventId);
}
