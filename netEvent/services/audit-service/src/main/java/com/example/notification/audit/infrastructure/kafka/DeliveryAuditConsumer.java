package com.example.notification.audit.infrastructure.kafka;

import com.example.notification.audit.domain.AuditHistoryEntity;
import com.example.notification.audit.infrastructure.AuditHistoryRepository;
import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.audit.DeliveryAuditEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class DeliveryAuditConsumer {
    private final AuditHistoryRepository repository;
    private final ObjectMapper objectMapper;

    public DeliveryAuditConsumer(AuditHistoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = NotificationTopics.AUDIT_LIFECYCLE, groupId = "audit-service-group")
    public void consumeLifecycle(Object message) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(message);
            log.info("AuditService received lifecycle audit log: {}", jsonPayload);

            UUID taskId = UUID.randomUUID();
            String channelCode = "AUDIT";
            String contact = "SYSTEM";
            String respCode = "200";

            if (message instanceof DeliveryAuditEvent) {
                DeliveryAuditEvent dae = (DeliveryAuditEvent) message;
                taskId = dae.getTaskId() != null ? dae.getTaskId() : taskId;
                channelCode = dae.getChannelCode() != null ? dae.getChannelCode() : channelCode;
                contact = dae.getRecipientContact() != null ? dae.getRecipientContact() : contact;
                respCode = dae.getResponseCode() != null ? dae.getResponseCode() : respCode;
            } else if (message instanceof LifecycleEvent) {
                LifecycleEvent le = (LifecycleEvent) message;
                channelCode = le.getServiceName() != null ? le.getServiceName() : channelCode;
                contact = le.getStage() != null ? le.getStage() : contact;
                respCode = le.getStatus() != null ? le.getStatus() : respCode;
            }

            repository.save(AuditHistoryEntity.builder()
                    .auditHistoryId(UUID.randomUUID())
                    .taskId(taskId)
                    .channelCode(channelCode)
                    .recipientContact(contact)
                    .payloadSnapshot(jsonPayload)
                    .responseCode(respCode)
                    .responseBody(jsonPayload)
                    .createdAt(Instant.now())
                    .build());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit message", e);
        }
    }
}
