package com.example.notification.routing.infrastructure.repository;

import com.example.notification.routing.domain.entity.RecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, UUID> {
    List<RecipientEntity> findByUsername(String username);
    List<RecipientEntity> findByEmail(String email);
}
