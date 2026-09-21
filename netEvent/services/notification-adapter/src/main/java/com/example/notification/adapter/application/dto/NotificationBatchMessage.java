package com.example.notification.adapter.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBatchMessage {
    private String batchId;
    private Instant createdAt;
    private int totalEvents;
    private List<NotificationEventItem> events;
}
