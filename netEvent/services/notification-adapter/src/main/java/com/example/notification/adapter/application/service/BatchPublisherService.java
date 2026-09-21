package com.example.notification.adapter.application.service;

import com.example.notification.adapter.application.dto.NotificationBatchMessage;
import com.example.notification.adapter.application.dto.NotificationEventItem;
import com.example.notification.adapter.domain.EventEntity;
import com.example.notification.adapter.infrastructure.EventRepository;
import com.example.notification.adapter.infrastructure.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchPublisherService {

    private final EventRepository eventRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    private static final DateTimeFormatter BATCH_ID_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").withZone(ZoneId.systemDefault());

    @Transactional
    public void publishPendingBatch() {
        List<EventEntity> pendingEvents = eventRepository.findTop500ByStatusOrderByReceivedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        String timestampPart = BATCH_ID_FORMATTER.format(Instant.now());
        String batchId = "BATCH-" + timestampPart + "-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Batch Publisher fetched {} PENDING records. Packaging into batchId: {}", pendingEvents.size(), batchId);

        List<NotificationEventItem> eventItems = new ArrayList<>();
        for (EventEntity entity : pendingEvents) {
            eventItems.add(NotificationEventItem.builder()
                    .eventId(entity.getNotificationEventId())
                    .profileId(entity.getProfileId())
                    .triggeredAt(entity.getReceivedAt())
                    .rawPayload(entity.getRawEventPayload())
                    .build());
        }

        NotificationBatchMessage batchMessage = NotificationBatchMessage.builder()
                .batchId(batchId)
                .createdAt(Instant.now())
                .totalEvents(eventItems.size())
                .events(eventItems)
                .build();

        try {
            kafkaEventPublisher.publishBatchMessage(batchMessage);

            Instant now = Instant.now();
            for (EventEntity entity : pendingEvents) {
                entity.setStatus("PUBLISHED");
                entity.setBatchId(batchId);
                entity.setPublishedAt(now);
            }
            eventRepository.saveAll(pendingEvents);
            log.info("Successfully published batch {} with {} events to Kafka.", batchId, pendingEvents.size());
        } catch (Exception e) {
            log.error("Failed to publish batch {} to Kafka. Incrementing retry count.", batchId, e);
            for (EventEntity entity : pendingEvents) {
                entity.setRetryCount(entity.getRetryCount() + 1);
                entity.setErrorMessage(e.getMessage());
            }
            eventRepository.saveAll(pendingEvents);
        }
    }
}
