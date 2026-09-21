package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    Optional<EventEntity> findByEventCode(String eventCode);
}
