package com.example.notification.sms.application;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.audit.DeliveryAuditEvent;
import com.example.notification.contract.audit.LifecycleEvent;
import com.example.notification.contract.delivery.DeliveryTaskEvent;
import com.example.notification.sms.infrastructure.persistence.DeliveryLogRepository;
import com.example.notification.sms.infrastructure.provider.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class SmsDeliveryService {

    private final SmsProvider smsProvider;
    private final DeliveryLogRepository deliveryLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SmsDeliveryService(SmsProvider smsProvider, DeliveryLogRepository deliveryLogRepository,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.smsProvider = smsProvider;
        this.deliveryLogRepository = deliveryLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processDeliveryTask(DeliveryTaskEvent event) {
        Instant startedAt = Instant.now();
        log.info("SmsWorker executing task: taskId={}, recipient={}, attempt={}",
                event.getTaskId(), event.getRecipientTarget(), event.getAttemptNo());

        boolean success = smsProvider.sendSms(event.getRecipientTarget(), event.getBody());
        Instant finishedAt = Instant.now();

        deliveryLogRepository.logAttempt(
                event.getTaskId(),
                "SMS",
                "VIETTEL_SMS",
                success ? 200 : 500,
                success ? "SUCCESS" : "FAILED",
                startedAt,
                finishedAt
        );

        DeliveryAuditEvent auditEvent = new DeliveryAuditEvent(
                event.getTaskId(), "SMS", event.getRecipientTarget(),
                success ? "SENT" : "FAILED", "VIETTEL_SMS",
                success ? "200" : "500",
                success ? "Delivered successfully" : "SMS Gateway timeout",
                finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getTaskId().toString(), auditEvent);

        LifecycleEvent lifecycleEvent = new LifecycleEvent(
                event.getCorrelationId(), event.getRunId(), "SmsTaskProcessed", "sms-worker",
                "DELIVERY", success ? "SENT" : "FAILED", "SYSTEM",
                Map.of("taskId", event.getTaskId().toString(), "channel", "SMS", "target", event.getRecipientTarget() != null ? event.getRecipientTarget() : ""), finishedAt
        );
        kafkaTemplate.send(NotificationTopics.AUDIT_LIFECYCLE, event.getCorrelationId() != null ? event.getCorrelationId().toString() : event.getTaskId().toString(), lifecycleEvent);
    }
}
