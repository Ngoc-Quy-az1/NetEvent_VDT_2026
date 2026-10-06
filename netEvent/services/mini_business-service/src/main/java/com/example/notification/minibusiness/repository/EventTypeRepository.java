package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.EventTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventTypeRepository extends JpaRepository<EventTypeEntity, UUID> {
    Optional<EventTypeEntity> findByEventTypeCode(String eventTypeCode);
}
