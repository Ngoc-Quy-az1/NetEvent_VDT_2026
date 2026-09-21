package com.example.notification.adapter.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreatedEvent {
    private UUID eventId;
    private String eventCode;
    private String eventName;
    private String type;
    private String severity;
    private Map<String, Object> payload;
    private Instant timestamp;
}
