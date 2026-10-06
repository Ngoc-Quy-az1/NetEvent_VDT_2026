package com.example.notification.adapter.application.service;

import com.example.notification.adapter.application.dto.NotificationEventItem;
import com.example.notification.adapter.domain.NotificationEventEntity;
import com.example.notification.adapter.infrastructure.repository.NotificationEventRepository;
import com.example.notification.contract.profile.ProfileEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    private final NotificationEventRepository notificationEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void collect(NotificationEventItem item) {
        try {
            UUID notificationEventId = item.getNotificationEventId() != null 
                    ? item.getNotificationEventId() 
                    : UUID.randomUUID();

            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> profile = new HashMap<>();
            profile.put("profileName", item.getProfileName());
            profile.put("displayName", item.getDisplayName());
            profile.put("channelCodes", item.getProfileChannelNames());
            payload.put("profile", profile);
            payload.put("eventSessionCodes", item.getEventSessionCodes());
            if (item.getEventName() != null && !item.getEventName().isBlank()) {
                payload.put("eventName", item.getEventName());
            }
            payload.put("templates", compactTemplates(item));
            payload.put("accounts", compactAccounts(item));
            payload.put("profileGroups", item.getProfileGroups());
            if (item.getEventId() != null) {
                payload.put("eventId", item.getEventId());
            }

            ProfileEvent profileEvent = new ProfileEvent(
                    item.getProfileId(),
                    notificationEventId,
                    "PROFILE_TRIGGERED",
                    item.getTriggeredAt() != null ? item.getTriggeredAt() : Instant.now(),
                    payload
            );

            NotificationEventEntity eventEntity = NotificationEventEntity.builder()
                    .notificationEventId(notificationEventId)
                    .profileId(item.getProfileId())
                    .eventId(item.getEventId())
                    .rawEventPayload(objectMapper.writeValueAsString(profileEvent))
                    .status("PENDING")
                    .receivedAt(Instant.now())
                    .retryCount(0)
                    .build();

            notificationEventRepository.save(eventEntity);
            log.info("[NOTIFICATION_EVENT] Successfully saved to notification_event table. notificationEventId={}, profileId={}, eventId={}",
                    notificationEventId, item.getProfileId(), item.getEventId());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot persist notification event " + item.getNotificationEventId(), exception);
        }
    }

    private List<Map<String, Object>> compactAccounts(NotificationEventItem item) {
        List<Map<String, Object>> accounts = new ArrayList<>();
        if (item.getAccounts() == null) return accounts;
        item.getAccounts().forEach(account -> {
            Map<String, Object> value = new HashMap<>();
            value.put("accountId", account.getAccountId());
            value.put("fullName", account.getFullName());
            value.put("username", account.getUsername());
            value.put("label", account.getLabel());
            value.put("channelId", account.getChannelId());
            value.put("contactValue", account.getContactValue());
            value.put("isActive", account.getIsActive());
            value.put("config", jsonObject(account.getConfig()));
            accounts.add(value);
        });
        return accounts;
    }

    private List<Map<String, Object>> compactTemplates(NotificationEventItem item) {
        List<Map<String, Object>> templates = new ArrayList<>();
        if (item.getTemplates() == null) return templates;
        item.getTemplates().stream().filter(template -> "ACTIVE".equalsIgnoreCase(template.getStatus())).forEach(template -> {
            Map<String, Object> config = jsonObject(template.getConfig());
            Map<String, Object> value = new HashMap<>();
            value.put("templateId", template.getTemplateId());
            value.put("templateName", template.getTemplateName());
            value.put("channelId", template.getChannelId());
            value.put("channelCode", template.getChannel() == null ? null : template.getChannel().getChannelName());
            value.put("format", config.get("format"));
            value.put("title", config.get("title"));
            value.put("content", config.containsKey("content") ? config.get("content") : config.get("body"));
            templates.add(value);
        });
        return templates;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonObject(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try { return objectMapper.readValue(json, Map.class); }
        catch (Exception ignored) { return new HashMap<>(); }
    }
}
