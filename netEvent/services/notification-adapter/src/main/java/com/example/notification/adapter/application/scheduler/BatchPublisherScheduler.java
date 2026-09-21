package com.example.notification.adapter.application.scheduler;

import com.example.notification.adapter.application.service.BatchPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchPublisherScheduler {

    private final BatchPublisherService batchPublisherService;

    @Scheduled(cron = "${scheduler.publish-cron:0 * * * * *}")
    public void publishBatch() {
        batchPublisherService.publishPendingBatch();
    }
}
