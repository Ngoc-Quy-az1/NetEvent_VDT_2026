package com.example.notification.adapter.application.scheduler;

import com.example.notification.adapter.application.service.ProfileTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor

@ConditionalOnProperty(
        name = "scheduler.profile.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ProfileTriggerScheduler {
    private final ProfileTriggerService profileTriggerService;

    @Scheduled(
            cron = "${scheduler.profile.scan-cron:0 * * * * *}",
            zone = "${scheduler.profile.zone:Asia/Ho_Chi_Minh}"
    )
    public void scanDueProfiles() {
        try {
            profileTriggerService.processDueProfiles();
        } catch (Exception exception) {
        }
    }
}
