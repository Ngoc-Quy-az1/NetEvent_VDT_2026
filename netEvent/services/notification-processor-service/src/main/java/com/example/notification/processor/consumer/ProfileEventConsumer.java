package com.example.notification.processor.consumer;

import com.example.notification.common.kafka.NotificationTopics;
import com.example.notification.contract.profile.ProfileEvent;
import com.example.notification.processor.application.ProfileNotificationProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProfileEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProfileEventConsumer.class);

    private final ProfileProcessingResultPublisher resultPublisher;
    private final ProfileNotificationProcessingService processingService;

    public ProfileEventConsumer(ProfileProcessingResultPublisher resultPublisher,
                                ProfileNotificationProcessingService processingService) {
        this.resultPublisher = resultPublisher;
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = NotificationTopics.PROFILE_TRIGGERED,
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${processor.kafka.concurrency:3}")
    public void consume(ProfileEvent event) {
        if (event == null || event.getCorrelationId() == null) {
            throw new IllegalArgumentException("ProfileEvent must include correlationId");
        }
        log.info("Received ProfileEvent on topic {}: correlationId={}, profileId={}",
                NotificationTopics.PROFILE_TRIGGERED, event.getCorrelationId(), event.getProfileId());
        processingService.process(event);
        resultPublisher.publishProcessed(event.getCorrelationId());
        log.info("Published PROCESSED acknowledgement: correlationId={}", event.getCorrelationId());
    }
}
