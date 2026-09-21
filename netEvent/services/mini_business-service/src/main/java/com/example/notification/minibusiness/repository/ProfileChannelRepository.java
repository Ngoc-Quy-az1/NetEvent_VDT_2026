package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ProfileChannelEntity;
import com.example.notification.minibusiness.domain.entity.ProfileChannelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileChannelRepository extends JpaRepository<ProfileChannelEntity, ProfileChannelId> {
    List<ProfileChannelEntity> findByProfileId(UUID profileId);
    void deleteByProfileId(UUID profileId);
}
