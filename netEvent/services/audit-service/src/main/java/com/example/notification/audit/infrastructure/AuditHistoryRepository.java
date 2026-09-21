package com.example.notification.audit.infrastructure;

import com.example.notification.audit.domain.AuditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditHistoryRepository extends JpaRepository<AuditHistoryEntity, UUID> {
    List<AuditHistoryEntity> findByTaskId(UUID taskId);
    List<AuditHistoryEntity> findByChannelCode(String channelCode);
}

