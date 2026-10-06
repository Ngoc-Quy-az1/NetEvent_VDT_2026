package com.example.notification.adapter.infrastructure.kafka;

import com.example.notification.adapter.domain.NotificationEventEntity;
import com.example.notification.adapter.infrastructure.repository.NotificationEventRepository;
import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.profile.ProfileProcessingResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProfileProcessingResultConsumer {
    private final NotificationEventRepository repository;
    public ProfileProcessingResultConsumer(NotificationEventRepository repository) { this.repository = repository; }
    @KafkaListener(topics = NotificationTopics.PROFILE_PROCESSING_RESULT,
                   groupId = "${notification.adapter.result-consumer.group-id:notification-adapter-result-group}",
                   containerFactory = "resultKafkaListenerContainerFactory")
    @Transactional
    public void consume(ProfileProcessingResultEvent result) {
        if (result.getCorrelationId() == null) {
            log.warn("Ignoring processing result without correlationId: eventId={}", result.getEventId());
            return;
        }
        repository.findByNotificationEventId(result.getCorrelationId())
                .ifPresentOrElse(event -> update(event, result),
                        () -> log.warn("Notification event not found for processing result: correlationId={}, eventId={}",
                                result.getCorrelationId(), result.getEventId()));
    }
    private void update(NotificationEventEntity event, ProfileProcessingResultEvent result) {
        event.setStatus("PROCESSED");
        event.setProcessedAt(result.getProcessedAt());
        event.setErrorMessage(null);
        repository.save(event);
    }
}
