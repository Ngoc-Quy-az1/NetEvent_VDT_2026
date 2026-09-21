package com.example.notification.ott.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.audit.DeliveryAuditEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.ott.infrastructure.persistence.DeliveryLogRepository;
import com.example.notification.ott.infrastructure.provider.OttProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class OttDeliveryService {

    private final OttProvider ottProvider;
    private final DeliveryLogRepository deliveryLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OttDeliveryService(OttProvider ottProvider, DeliveryLogRepository deliveryLogRepository,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.ottProvider = ottProvider;
        this.deliveryLogRepository = deliveryLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processDeliveryTask(DeliveryTaskEvent event) {
        Instant startedAt = Instant.now();
        log.info("OttWorker executing task: taskId={}, recipient={}, attempt={}",
                event.getTaskId(), event.getRecipientTarget(), event.getAttemptNo());

        boolean success = ottProvider.sendOtt(event.getRecipientTarget(), event.getBody());
        Instant finishedAt = Instant.now();

        deliveryLogRepository.logAttempt(
                event.getTaskId(),
                "OTT",
                "TELEGRAM",
                success ? 200 : 500,
                success ? "SUCCESS" : "FAILED",
                startedAt,
                finishedAt
        );

        DeliveryAuditEvent auditEvent = new DeliveryAuditEvent(
                event.getTaskId(), "OTT", event.getRecipientTarget(),
                success ? "SENT" : "FAILED", "TELEGRAM",
                success ? "200" : "500",
                success ? "Delivered successfully" : "Telegram Bot API error",
                finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getTaskId().toString(), auditEvent);

        LifecycleEvent lifecycleEvent = new LifecycleEvent(
                event.getCorrelationId(), event.getRunId(), "OttTaskProcessed", "ott-worker",
                "DELIVERY", success ? "SENT" : "FAILED", "SYSTEM",
                Map.of("taskId", event.getTaskId().toString(), "channel", "OTT", "target", event.getRecipientTarget() != null ? event.getRecipientTarget() : ""), finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getCorrelationId() != null ? event.getCorrelationId().toString() : event.getTaskId().toString(), lifecycleEvent);
    }
}
