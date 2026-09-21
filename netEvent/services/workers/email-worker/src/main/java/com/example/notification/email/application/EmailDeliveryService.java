package com.example.notification.email.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.audit.DeliveryAuditEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.email.infrastructure.persistence.DeliveryLogRepository;
import com.example.notification.email.infrastructure.provider.EmailProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class EmailDeliveryService {

    private final EmailProvider emailProvider;
    private final DeliveryLogRepository deliveryLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EmailDeliveryService(EmailProvider emailProvider, DeliveryLogRepository deliveryLogRepository,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.emailProvider = emailProvider;
        this.deliveryLogRepository = deliveryLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processDeliveryTask(DeliveryTaskEvent event) {
        Instant startedAt = Instant.now();
        log.info("EmailWorker executing task: taskId={}, recipient={}, attempt={}",
                event.getTaskId(), event.getRecipientTarget(), event.getAttemptNo());

        boolean success = emailProvider.sendEmail(event.getRecipientTarget(), event.getTitle(), event.getBody());
        Instant finishedAt = Instant.now();

        deliveryLogRepository.logAttempt(
                event.getTaskId(),
                "EMAIL",
                "SMTP",
                success ? 200 : 500,
                success ? "SUCCESS" : "FAILED",
                startedAt,
                finishedAt
        );

        DeliveryAuditEvent auditEvent = new DeliveryAuditEvent(
                event.getTaskId(), "EMAIL", event.getRecipientTarget(),
                success ? "SENT" : "FAILED", "SMTP",
                success ? "200" : "500",
                success ? "Delivered successfully" : "SMTP Provider connection error",
                finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getTaskId().toString(), auditEvent);

        LifecycleEvent lifecycleEvent = new LifecycleEvent(
                event.getCorrelationId(), event.getRunId(), "EmailTaskProcessed", "email-worker",
                "DELIVERY", success ? "SENT" : "FAILED", "SYSTEM",
                Map.of("taskId", event.getTaskId().toString(), "channel", "EMAIL", "target", event.getRecipientTarget() != null ? event.getRecipientTarget() : ""), finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getCorrelationId() != null ? event.getCorrelationId().toString() : event.getTaskId().toString(), lifecycleEvent);
    }
}
