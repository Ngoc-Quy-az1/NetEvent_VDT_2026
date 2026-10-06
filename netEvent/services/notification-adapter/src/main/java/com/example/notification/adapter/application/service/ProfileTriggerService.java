package com.example.notification.adapter.application.service;

import com.example.notification.adapter.application.dto.AccountDTO;
import com.example.notification.adapter.application.dto.NotificationEventItem;
import com.example.notification.adapter.domain.EventSessionEntity;
import com.example.notification.adapter.domain.ProfileEntity;
import com.example.notification.adapter.domain.TemplateEntity;
import com.example.notification.adapter.infrastructure.repository.AccountRepository;
import com.example.notification.adapter.infrastructure.repository.EventSessionRepository;
import com.example.notification.adapter.infrastructure.repository.ProfileRepository;
import com.example.notification.adapter.infrastructure.repository.ProfileGroupQueryRepository;
import com.example.notification.adapter.infrastructure.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileTriggerService {
    private final ProfileRepository profileRepository;
    private final TemplateRepository templateRepository;
    private final EventSessionRepository eventSessionRepository;
    private final AccountRepository accountRepository;
    private final CollectorService collectorService;
    private final ProfileGroupQueryRepository profileGroupQueryRepository;

    @Value("${scheduler.profile.zone:Asia/Ho_Chi_Minh}")
    private String schedulerZone;

    @Transactional
    public void processDueProfiles() {
        if (!Boolean.TRUE.equals(profileRepository.tryAcquireTriggerLock())) {
            return;
        }
        Instant now = Instant.now();
        Instant scheduledMinute = now.truncatedTo(ChronoUnit.MINUTES);
        List<ProfileEntity> profiles = profileRepository.findEligibleProfilesForTrigger(now);
        for (ProfileEntity profile : profiles) {
            try {
                if (isDue(profile.getCronExpression(), scheduledMinute)) {
                    processProfile(profile, scheduledMinute);
                }
            } catch (Exception exception) {
            }
        }
    }

    private void processProfile(ProfileEntity profile, Instant triggeredAt) {
        List<EventSessionEntity> eventSessions = eventSessionRepository.findByProfileId(profile.getProfileId());
        List<TemplateEntity> templates = templateRepository.findEventTemplatesForProfile(profile.getProfileId());
        List<AccountDTO> account = accountRepository.findAccountsByProfileId(profile.getProfileId());
        UUID eventId = eventSessions.isEmpty() ? null : eventSessions.get(0).getEventId();
        String eventName = eventSessions.isEmpty() || eventSessions.get(0).getEvent() == null
                ? null : eventSessions.get(0).getEvent().getEventName();

        NotificationEventItem event = NotificationEventItem.builder()
                .notificationEventId(UUID.randomUUID())
                .eventId(eventId)
                .eventName(eventName)
                .profileId(profile.getProfileId())
                .profileName(profile.getProfileName())
                .displayName(profile.getDisplayName())
                .displayNameUi(profile.getDisplayNameUi())
                .profileStatus(profile.getStatus())
                .cronExpression(profile.getCronExpression())
                .profileStartTime(profile.getStartTime())
                .profileEndTime(profile.getEndTime())
                .profileCreatedAt(profile.getCreatedAt())
                .profileUpdatedAt(profile.getUpdatedAt())
                .profileChannelIds(profile.getProfileChannels().stream()
                        .map(profileChannel -> profileChannel.getChannelId())
                        .collect(Collectors.toList()))
                .profileChannelNames(profile.getProfileChannels().stream()
                        .map(profileChannel -> profileChannel.getChannel().getChannelName())
                        .collect(Collectors.toList()))
                .triggeredAt(triggeredAt)
                .eventSessionCodes(eventSessions.stream().map(EventSessionEntity::getSessionEventCode).collect(Collectors.toList()))
                .templates(templates)
                .accounts(account)
                .profileGroups(profileGroupQueryRepository.findByProfileId(profile.getProfileId()))
                .build();

        collectorService.collect(event);
    }

    private boolean isDue(String expression, Instant scheduledMinute) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        ZonedDateTime candidate = CronExpression.parse(normalizeCronExpression(expression))
                .next(scheduledMinute.minusSeconds(1).atZone(ZoneId.of(schedulerZone)));
        return candidate != null && candidate.toInstant().equals(scheduledMinute);
    }

    private String normalizeCronExpression(String expression) {
        String normalized = expression.trim().replaceAll("\\s+", " ");
        String[] fields = normalized.split(" ");
        if (fields.length == 5) {
            return "0 " + normalized;
        }
        if (fields.length != 6) {
            throw new IllegalArgumentException("Cron expression must have 5 or 6 fields: " + expression);
        }
        return normalized;
    }
}
