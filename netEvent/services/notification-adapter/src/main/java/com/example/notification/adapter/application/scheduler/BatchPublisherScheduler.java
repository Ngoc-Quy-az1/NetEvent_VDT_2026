package com.example.notification.adapter.application.scheduler;

import com.example.notification.adapter.application.service.BatchPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.batch.enabled", havingValue = "true", matchIfMissing = true)
public class BatchPublisherScheduler {

    private final BatchPublisherService batchPublisherService;

    @Scheduled(fixedDelayString = "${scheduler.batch.fixed-delay:10000}")
    public void publishBatch() {
        try {
            batchPublisherService.publishPendingEvents();
        } catch (Exception exception) {
            log.error("[BATCH-SCHEDULER] Failed publishing batch", exception);
        }
    }
}
