package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, UUID> {
    Optional<ChannelEntity> findByChannelName(String channelName);
}
