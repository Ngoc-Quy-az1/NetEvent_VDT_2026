package com.example.notification.processor.consumer;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.profile.ProfileProcessingResultEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;

@Component
public class ProfileProcessingResultPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public ProfileProcessingResultPublisher(KafkaTemplate<String, Object> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }
    public void publishProcessed(UUID correlationId) {
        try {
            kafkaTemplate.send(NotificationTopics.PROFILE_PROCESSING_RESULT, correlationId.toString(),
                    new ProfileProcessingResultEvent(correlationId, correlationId, "PROCESSED", null, Instant.now())).get();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot publish PROCESSED acknowledgement for " + correlationId, exception);
        }
    }
}
