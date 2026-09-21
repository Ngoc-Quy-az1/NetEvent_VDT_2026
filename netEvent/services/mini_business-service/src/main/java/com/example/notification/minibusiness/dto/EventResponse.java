package com.example.notification.minibusiness.dto;

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
public class EventResponse {

    private UUID eventId;
    private String eventCode;
    private String eventName;
    private UUID eventTypeId;
    private UUID holidayId;
    private String eventLevel;
    private Boolean annual;
    private Boolean isLunar;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
