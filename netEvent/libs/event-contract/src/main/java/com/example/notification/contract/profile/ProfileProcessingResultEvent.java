package com.example.notification.contract.profile;

import java.time.Instant;
import java.util.UUID;

public class ProfileProcessingResultEvent {
    private UUID eventId;
    private UUID correlationId;
    private String status;
    private String errorMessage;
    private Instant processedAt;
    public ProfileProcessingResultEvent() { }
    public ProfileProcessingResultEvent(UUID eventId, UUID correlationId, String status, String errorMessage, Instant processedAt) {
        this.eventId = eventId; this.correlationId = correlationId; this.status = status;
        this.errorMessage = errorMessage; this.processedAt = processedAt;
    }
    public UUID getEventId() { return eventId; } public void setEventId(UUID v) { eventId = v; }
    public UUID getCorrelationId() { return correlationId; } public void setCorrelationId(UUID v) { correlationId = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getErrorMessage() { return errorMessage; } public void setErrorMessage(String v) { errorMessage = v; }
    public Instant getProcessedAt() { return processedAt; } public void setProcessedAt(Instant v) { processedAt = v; }
}
