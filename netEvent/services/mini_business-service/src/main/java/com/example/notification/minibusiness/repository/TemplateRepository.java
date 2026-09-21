package com.example.notification.minibusiness.repository;

import com.example.notification.minibusiness.domain.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, UUID> {
    List<TemplateEntity> findByChannelId(UUID channelId);
    List<TemplateEntity> findByStatus(String status);
}
