package com.example.notification.adapter.infrastructure;

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
        log.info("Publishing ProfileEvent to topic {}: correlationId={}", profileTriggeredTopic, event.getCorrelationId());
        kafkaTemplate.send(profileTriggeredTopic, event.getCorrelationId().toString(), event);
    }

    public void publishBatchMessage(NotificationBatchMessage batchMessage) {
        log.info("Publishing NotificationBatchMessage to topic {}: batchId={}, totalEvents={}",
                profileTriggeredTopic, batchMessage.getBatchId(), batchMessage.getTotalEvents());
        kafkaTemplate.send(profileTriggeredTopic, batchMessage.getBatchId(), batchMessage);
    }
}
