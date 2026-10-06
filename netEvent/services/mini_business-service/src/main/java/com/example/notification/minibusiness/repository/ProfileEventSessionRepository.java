package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ProfileEventSessionEntity;
import com.example.notification.minibusiness.domain.entity.ProfileEventSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileEventSessionRepository extends JpaRepository<ProfileEventSessionEntity, ProfileEventSessionId> {
    List<ProfileEventSessionEntity> findByProfileId(UUID profileId);
    void deleteByProfileId(UUID profileId);
}
