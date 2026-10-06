package com.example.notification.contract.profile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ProfileEvent {
    private UUID profileId;
    private UUID correlationId;
    private String eventType;
    private Instant triggeredAt;
    private Map<String, Object> payload;

    public ProfileEvent() {}

    public ProfileEvent(UUID profileId, UUID correlationId, String eventType, Instant triggeredAt, Map<String, Object> payload) {
        this.profileId = profileId;
        this.correlationId = correlationId;
        this.eventType = eventType;
        this.triggeredAt = triggeredAt;
        this.payload = payload;
    }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public UUID profileId() { return profileId; }
    public UUID correlationId() { return correlationId; }
    public String eventType() { return eventType; }
    public Instant triggeredAt() { return triggeredAt; }
    public Map<String, Object> payload() { return payload; }
}
