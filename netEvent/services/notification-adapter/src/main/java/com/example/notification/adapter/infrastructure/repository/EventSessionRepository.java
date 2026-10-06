package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.domain.EventSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventSessionRepository extends JpaRepository<EventSessionEntity, UUID> {
    List<EventSessionEntity> findByEventId(UUID eventId);

    @Query("SELECT es FROM EventSessionEntity es " +
           "JOIN es.profileEventSessions mapping " +
           "WHERE mapping.profileId = :profileId")
    List<EventSessionEntity> findByProfileId(@Param("profileId") UUID profileId);
}
