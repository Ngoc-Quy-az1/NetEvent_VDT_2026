package com.example.notification.processor.infrastructure.kafka;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.profile.ProfileEvent;
import com.example.notification.processor.application.EventProcessorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProfileEventConsumer {

    private final EventProcessorService eventProcessorService;

    public ProfileEventConsumer(EventProcessorService eventProcessorService) {
        this.eventProcessorService = eventProcessorService;
    }

    @KafkaListener(topics = NotificationTopics.PROFILE_TRIGGERED, groupId = "event-processor-group")
    public void consume(ProfileEvent event) {
        eventProcessorService.processProfileEvent(event);
    }
}
