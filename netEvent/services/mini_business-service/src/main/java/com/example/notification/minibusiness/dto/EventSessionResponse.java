package com.example.notification.minibusiness.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EventSessionResponse {
    private UUID sessionId;
    private String sessionEventCode;
    private UUID eventId;
    private Instant startDate;
    private Instant endDate;
    private String status;
}
