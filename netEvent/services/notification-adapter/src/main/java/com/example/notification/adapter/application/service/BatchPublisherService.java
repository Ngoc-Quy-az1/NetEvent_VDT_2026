package com.example.notification.adapter.application.service;

import com.example.notification.adapter.domain.NotificationEventEntity;
import com.example.notification.adapter.infrastructure.kafka.KafkaEventPublisher;
import com.example.notification.adapter.infrastructure.repository.NotificationEventRepository;
import com.example.notification.contract.profile.ProfileEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchPublisherService {

    private static final int MAX_RETRY_COUNT = 5;
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(5);
    private final NotificationEventRepository notificationEventRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${scheduler.batch.size:20}")
    private int batchSize;

    @Transactional
    public void publishPendingEvents() {
        List<NotificationEventEntity> events = claimPendingEvents();
        if (events.isEmpty()) {
            return;
        }

        for (NotificationEventEntity event : events) {
            try {
                ProfileEvent profileEvent = objectMapper.readValue(event.getRawEventPayload(), ProfileEvent.class);
                if (profileEvent.getCorrelationId() == null) {
                    profileEvent.setCorrelationId(event.getNotificationEventId());
                }
                if (profileEvent.getProfileId() == null) {
                    profileEvent.setProfileId(event.getProfileId());
                }
                kafkaEventPublisher.publishProfileTriggeredEventAndWait(profileEvent);
                markPublished(event);
            } catch (Exception exception) {
                markFailedAttempt(event, exception);
                log.error("[BATCH] Failed publishing eventId={}", event.getNotificationEventId(), exception);
            }
        }
    }

    @Transactional
    public List<NotificationEventEntity> claimPendingEvents() {
        notificationEventRepository.requeueExpiredProcessingEvents(Instant.now().minus(PROCESSING_TIMEOUT));
        List<NotificationEventEntity> events = notificationEventRepository.findByStatusOrderByReceivedAtAsc(
                "PENDING", PageRequest.of(0, Math.max(1, batchSize)));
        Instant claimedAt = Instant.now();
        for (NotificationEventEntity event : events) {
            event.setStatus("PROCESSING");
            event.setProcessedAt(claimedAt);
        }
        return events;
    }

    @Transactional
    public void markPublished(NotificationEventEntity event) {
        event.setStatus("PUBLISHED");
        event.setPublishedAt(Instant.now());
        event.setErrorMessage(null);
        notificationEventRepository.save(event);
    }

    @Transactional
    public void markFailedAttempt(NotificationEventEntity event, Exception exception) {
        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);
        event.setErrorMessage(exception.getMessage());
        event.setProcessedAt(null);
        event.setStatus(retryCount >= MAX_RETRY_COUNT ? "FAILED" : "PENDING");
        notificationEventRepository.save(event);
    }
}
