package com.example.notification.adapter.application.scheduler;

import com.example.notification.adapter.application.service.CollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCollector {

    private final CollectorService collectorService;

    @Scheduled(cron = "${scheduler.collect-cron:0 13 * * * *}")
    public void collectProfiles() {
        log.info("Triggered Scheduled Profile Collector (cron: ${scheduler.collect-cron:0 13 * * * *})");
        collectorService.collectActiveProfiles();
    }
}
