package com.example.notification.processor.application;

import com.example.notification.contract.profile.ProfileEvent;
import com.example.notification.processor.entity.Notification;
import com.example.notification.processor.entity.NotificationGroup;
import com.example.notification.processor.entity.ProcessedEvent;
import com.example.notification.processor.entity.Recipient;
import com.example.notification.processor.repository.NotificationGroupRepository;
import com.example.notification.processor.repository.NotificationRepository;
import com.example.notification.processor.repository.ProcessedEventRepository;
import com.example.notification.processor.repository.RecipientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Stores the original Kafka message only. No template, business-rule, or delivery processing is performed. */
@Service
@RequiredArgsConstructor
public class ProfileNotificationProcessingService {
    private static final String CONSUMER_NAME = "notification-processor-service";

    private final ProcessedEventRepository processedEventRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationGroupRepository notificationGroupRepository;
    private final RecipientRepository recipientRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(ProfileEvent event) {
        UUID correlationId = event.getCorrelationId();
        if (processedEventRepository.existsById(correlationId)) {
            return;
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .notificationEventId(correlationId)
                .profileId(event.getProfileId())
                .rawDataJson(toJson(event))
                .status("RAW_STORED")
                .build());

        persistTargets(notification.getRawDataJson());
        notification.setStatus("RECIPIENTS_AND_GROUPS_STORED");
        notificationRepository.save(notification);

        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(correlationId)
                .consumerName(CONSUMER_NAME)
                .eventType(event.getEventType())
                .processedAt(Instant.now())
                .build());
    }

    /** Replays incomplete raw records when the processor starts after a deployment. */
    @Transactional
    public int persistStoredTargets() {
        List<Notification> notifications = notificationRepository.findByStatusIn(
                Set.of("RAW_STORED", "RECIPIENTS_STORED"));
        for (Notification notification : notifications) {
            if (blank(notification.getRawDataJson())) continue;
            persistTargets(notification.getRawDataJson());
            notification.setStatus("RECIPIENTS_AND_GROUPS_STORED");
        }
        return notifications.size();
    }

    private String toJson(ProfileEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot serialize raw ProfileEvent", exception);
        }
    }

    private void persistTargets(String rawDataJson) {
        try {
            JsonNode payload = objectMapper.readTree(rawDataJson).path("payload");
            persistRecipients(payload.path("accounts"));
            persistGroups(payload.path("profileGroups"));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot read recipients and groups from raw_data", exception);
        }
    }

    private void persistRecipients(JsonNode accounts) {
        if (!accounts.isArray()) return;

        for (JsonNode account : accounts) {
            if (!account.path("isActive").asBoolean(false)) continue;
            UUID accountId = uuid(text(account, "accountId"));
            String channelId = text(account, "channelId");
            String contactValue = text(account, "contactValue");
            if (accountId == null || blank(channelId) || blank(contactValue)) continue;

            Recipient recipient = recipientRepository.findByAccountIdAndStatus(accountId, "ACTIVE")
                    .stream().findFirst()
                    .orElseGet(() -> Recipient.builder()
                            .accountId(accountId)
                            .status("ACTIVE")
                            .channelConfig("{}")
                            .build());
            recipient.setFullName(firstText(account, "fullName", "displayName", "username", accountId.toString()));
            recipient.setChannelConfig(withAccountChannel(recipient.getChannelConfig(), account, channelId));
            if (contactValue.matches("^\\+?\\d{8,15}$")) recipient.setPhoneNumber(contactValue);
            recipientRepository.save(recipient);
        }
    }

    private void persistGroups(JsonNode groups) {
        if (!groups.isArray()) return;
        for (JsonNode group : groups) {
            UUID groupId = uuid(firstText(group, "group_id", "groupId"));
            if (groupId == null) continue;
            NotificationGroup notificationGroup = notificationGroupRepository.findById(groupId)
                    .orElseGet(() -> NotificationGroup.builder().groupId(groupId).status("ACTIVE").build());
            notificationGroup.setChannelId(uuid(firstText(group, "channel_id", "channelId")));
            notificationGroup.setGroupConfig(groupConfig(group.path("group_config")));
            notificationGroupRepository.save(notificationGroup);
        }
    }

    @SuppressWarnings("unchecked")
    private String withAccountChannel(String currentConfig, JsonNode account, String channelId) {
        try {
            Map<String, Object> config = currentConfig == null || currentConfig.isBlank()
                    ? new LinkedHashMap<>()
                    : objectMapper.readValue(currentConfig, new TypeReference<LinkedHashMap<String, Object>>() { });
            config.put("username", text(account, "username"));
            Map<String, Object> channels = config.get("channels") instanceof Map
                    ? new LinkedHashMap<>((Map<String, Object>) config.get("channels"))
                    : new LinkedHashMap<>();
            Map<String, Object> channel = new LinkedHashMap<>();
            channel.put("contactValue", text(account, "contactValue"));
            channel.put("label", text(account, "label"));
            channel.put("config", objectMapper.convertValue(account.path("config"), new TypeReference<Map<String, Object>>() { }));
            channels.put(channelId, channel);
            config.put("channels", channels);
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot update recipient channel configuration", exception);
        }
    }

    private String groupConfig(JsonNode rawConfig) {
        try {
            if (rawConfig.hasNonNull("value") && rawConfig.path("value").isTextual()) {
                return objectMapper.writeValueAsString(objectMapper.readTree(rawConfig.path("value").asText()));
            }
            return objectMapper.writeValueAsString(rawConfig.isMissingNode() || rawConfig.isNull()
                    ? new LinkedHashMap<>() : rawConfig);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Cannot read group_config from raw_data", exception);
        }
    }

    private String firstText(JsonNode node, String... names) {
        for (String name : names) {
            String value = text(node, name);
            if (!blank(value)) return value;
        }
        return "Unknown recipient";
    }

    private String text(JsonNode node, String name) {
        return node.hasNonNull(name) ? node.get(name).asText() : null;
    }

    private UUID uuid(String value) {
        try {
            return value == null || value.isBlank() ? null : UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
