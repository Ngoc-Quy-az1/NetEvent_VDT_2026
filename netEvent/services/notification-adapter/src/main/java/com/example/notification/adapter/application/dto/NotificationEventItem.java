package com.example.notification.adapter.application.dto;

import com.example.notification.adapter.domain.TemplateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventItem {
    private UUID notificationEventId;
    private UUID eventId;
    private String eventName;
    private UUID profileId;
    private String profileName;
    private String displayName;
    private String displayNameUi;
    private String profileStatus;
    private String cronExpression;
    private Instant profileStartTime;
    private Instant profileEndTime;
    private Instant profileCreatedAt;
    private Instant profileUpdatedAt;
    private List<UUID> profileChannelIds;
    private List<String> profileChannelNames;
    private Instant triggeredAt;
    private List<String> eventSessionCodes;
    private List<TemplateEntity> templates;
    private List<AccountDTO> accounts;
    private List<Map<String, Object>> profileGroups;

    public UUID getNotificationEventId() {
        return notificationEventId != null ? notificationEventId : eventId;
    }

    public static class NotificationEventItemBuilder {
        public NotificationEventItemBuilder account(List<AccountDTO> account) {
            this.accounts = account;
            return this;
        }
    }
}
