package com.example.notification.adapter.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventItem {
    private UUID eventId;
    private UUID profileId;
    private String profileName;
    private Instant triggeredAt;
    private String rawPayload;
}
