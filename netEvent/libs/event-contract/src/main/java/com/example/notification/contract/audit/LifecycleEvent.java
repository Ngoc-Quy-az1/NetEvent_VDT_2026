package com.example.notification.contract.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class LifecycleEvent {
    private UUID correlationId;
    private UUID runId;
    private String eventType;
    private String serviceName;
    private String stage;
    private String status;
    private String actor;
    private Map<String, Object> metadata;
    private Instant occurredAt;

    public LifecycleEvent() {}

    public LifecycleEvent(UUID correlationId, UUID runId, String eventType, String serviceName,
                          String stage, String status, String actor, Map<String, Object> metadata, Instant occurredAt) {
        this.correlationId = correlationId;
        this.runId = runId;
        this.eventType = eventType;
        this.serviceName = serviceName;
        this.stage = stage;
        this.status = status;
        this.actor = actor;
        this.metadata = metadata;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
