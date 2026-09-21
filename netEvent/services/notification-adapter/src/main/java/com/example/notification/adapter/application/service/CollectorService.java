package com.example.notification.adapter.application.service;

import com.example.notification.adapter.domain.EventEntity;
import com.example.notification.adapter.infrastructure.EventRepository;
import com.example.notification.adapter.infrastructure.client.BusinessClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    private final BusinessClient businessClient;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void collectActiveProfiles() {
        log.info("Starting Profile Collector execution...");
        List<BusinessClient.ProfileDto> activeProfiles = businessClient.getActiveProfiles();
        if (activeProfiles.isEmpty()) {
            log.info("No active profiles found from mini-business-service");
            return;
        }

        log.info("Collected {} active profiles. Saving to notification_event table with PENDING status...", activeProfiles.size());
        for (BusinessClient.ProfileDto profile : activeProfiles) {
            try {
                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("profileId", profile.getProfileId());
                payloadMap.put("profileName", profile.getProfileName());
                payloadMap.put("displayName", profile.getDisplayName());
                payloadMap.put("cronExpression", profile.getCronExpression());
                payloadMap.put("triggeredAt", Instant.now().toString());

                String rawPayload = objectMapper.writeValueAsString(payloadMap);

                EventEntity entity = EventEntity.builder()
                        .profileId(profile.getProfileId())
                        .rawEventPayload(rawPayload)
                        .status("PENDING")
                        .receivedAt(Instant.now())
                        .retryCount(0)
                        .build();

                eventRepository.save(entity);
            } catch (Exception e) {
                log.error("Error packaging and saving event for profile: {}", profile.getProfileName(), e);
            }
        }
        log.info("Profile Collector completed successfully.");
    }
}
