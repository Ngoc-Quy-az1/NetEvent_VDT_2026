package com.example.notification.contract.delivery;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class DeliveryTaskEvent {
    private UUID taskId;
    private UUID notificationId;
    private UUID correlationId;
    private UUID runId;
    private String channel;
    private String recipientId;
    private String recipientTarget;
    private String title;
    private String body;
    private String checksum;
    private Integer attemptNo;
    private Map<String, String> metadata;
    private Instant createdAt;

    public DeliveryTaskEvent() {}

    public DeliveryTaskEvent(UUID taskId, UUID notificationId, UUID correlationId, UUID runId,
                             String channel, String recipientId, String recipientTarget,
                             String title, String body, String checksum, Integer attemptNo,
                             Map<String, String> metadata, Instant createdAt) {
        this.taskId = taskId;
        this.notificationId = notificationId;
        this.correlationId = correlationId;
        this.runId = runId;
        this.channel = channel;
        this.recipientId = recipientId;
        this.recipientTarget = recipientTarget;
        this.title = title;
        this.body = body;
        this.checksum = checksum;
        this.attemptNo = attemptNo != null ? attemptNo : 1;
        this.metadata = metadata;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // Overloaded constructor for compatibility
    public DeliveryTaskEvent(UUID taskId, UUID notificationId, UUID correlationId, String channel,
                             String recipientTarget, String title, String body,
                             Map<String, String> metadata, Instant createdAt) {
        this(taskId, notificationId, correlationId, null, channel, null, recipientTarget, title, body, null, 1, metadata, createdAt);
    }

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }

    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }

    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientTarget() { return recipientTarget; }
    public void setRecipientTarget(String recipientTarget) { this.recipientTarget = recipientTarget; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public Integer getAttemptNo() { return attemptNo; }
    public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public UUID taskId() { return taskId; }
    public UUID notificationId() { return notificationId; }
    public UUID correlationId() { return correlationId; }
    public UUID runId() { return runId; }
    public String channel() { return channel; }
    public String recipientTarget() { return recipientTarget; }
    public String title() { return title; }
    public String body() { return body; }
    public String checksum() { return checksum; }
    public Map<String, String> metadata() { return metadata; }
    public Instant createdAt() { return createdAt; }
}
