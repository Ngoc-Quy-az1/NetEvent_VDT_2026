package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByProfileName(String profileName);
    boolean existsByProfileName(String profileName);
}
