package com.example.notification.adapter.application.service;

import com.example.notification.adapter.application.dto.request.ReportCreatedEvent;
import com.example.notification.adapter.application.dto.request.ProfileTriggerRequest;
import com.example.notification.adapter.domain.EventEntity;
import com.example.notification.adapter.infrastructure.EventRepository;
import com.example.notification.adapter.infrastructure.KafkaEventPublisher;
import com.example.notification.contract.profile.ProfileEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final EventRepository eventRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ReportCreatedEvent processKpiReport(ProfileTriggerRequest request) {
        log.info("Processing KPI report ingestion: eventCode={}", request.getEventCode());

        UUID eventId = UUID.randomUUID();
        String payloadJson = "";
        try {
            payloadJson = objectMapper.writeValueAsString(request.getPayload());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize KPI payload to JSON", e);
        }

        UUID profileId = request.getProfileName() != null
                ? UUID.nameUUIDFromBytes(request.getProfileName().getBytes(StandardCharsets.UTF_8))
                : UUID.randomUUID();

        EventEntity entity = EventEntity.builder()
                .notificationEventId(eventId)
                .profileId(profileId)
                .rawEventPayload(payloadJson)
                .status("RECEIVED")
                .receivedAt(Instant.now())
                .build();

        eventRepository.save(entity);

        ReportCreatedEvent event = ReportCreatedEvent.builder()
                .eventId(eventId)
                .eventCode(request.getEventCode())
                .eventName(request.getEventName() != null ? request.getEventName() : "KPI_THRESHOLD_BREACH")
                .type(request.getType())
                .severity(request.getSeverity())
                .payload(request.getPayload())
                .timestamp(Instant.now())
                .build();

        ProfileEvent profileEvent = new ProfileEvent(profileId, eventId,
                request.getType() == null ? "PROFILE_TRIGGERED" : request.getType(),
                event.getTimestamp(), request.getPayload());
        kafkaEventPublisher.publishProfileTriggeredEvent(profileEvent);
        return event;
    }
}
