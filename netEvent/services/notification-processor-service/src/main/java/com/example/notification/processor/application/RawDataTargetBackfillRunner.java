package com.example.notification.processor.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RawDataTargetBackfillRunner {
    private final ProfileNotificationProcessingService processingService;

    @EventListener(ApplicationReadyEvent.class)
    public void persistStoredTargets() {
        int processed = processingService.persistStoredTargets();
        log.info("Persisted recipients and groups for {} stored raw notification(s)", processed);
    }
}
