package com.example.notification.adapter.infrastructure.kafka;

import com.example.notification.adapter.application.dto.NotificationBatchMessage;
import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.profile.ProfileEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor    
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.profile-triggered:" + NotificationTopics.PROFILE_TRIGGERED + "}")
    private String profileTriggeredTopic;

    public void publishProfileTriggeredEvent(ProfileEvent event) {
        String key = event.getCorrelationId() != null
                ? event.getCorrelationId().toString()
                : (event.getProfileId() != null ? event.getProfileId().toString() : "UNKNOWN");
        log.info("Publishing ProfileEvent to topic {}: correlationId={}, key={}", profileTriggeredTopic, event.getCorrelationId(), key);
        kafkaTemplate.send(profileTriggeredTopic, key, event);
    }

    public void publishProfileTriggeredEventAndWait(ProfileEvent event) throws Exception {
        String key = event.getCorrelationId() != null
                ? event.getCorrelationId().toString()
                : (event.getProfileId() != null ? event.getProfileId().toString() : "UNKNOWN");
        log.info("Publishing scheduled ProfileEvent to topic {}: correlationId={}, key={}",
                profileTriggeredTopic, event.getCorrelationId(), key);
        kafkaTemplate.send(profileTriggeredTopic, key, event).get();
        log.info("[KAFKA] Successfully published event with key={}", key);
    }

    // public void publishBatchMessage(NotificationBatchMessage batchMessage) throws Exception {
    //     log.info("Publishing NotificationBatchMessage to topic {}: batchId={}, totalEvents={}",
    //             profileTriggeredTopic, batchMessage.getBatchId(), batchMessage.getTotalEvents());
    //     kafkaTemplate.send(profileTriggeredTopic, batchMessage.getBatchId(), batchMessage).get();
    //     log.info("[KAFKA] Successfully published batchId={}", batchMessage.getBatchId());
    // }
}
