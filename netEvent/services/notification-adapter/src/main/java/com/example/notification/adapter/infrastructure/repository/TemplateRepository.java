package com.example.notification.adapter.infrastructure.repository;

import com.example.notification.adapter.domain.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, UUID> {

    @Query("SELECT pc.template FROM ProfileChannelEntity pc " +
           "WHERE pc.profileId = :profileId AND pc.templateId IS NOT NULL")
    List<TemplateEntity> findEventTemplatesForProfile(@Param("profileId") UUID profileId);
}
