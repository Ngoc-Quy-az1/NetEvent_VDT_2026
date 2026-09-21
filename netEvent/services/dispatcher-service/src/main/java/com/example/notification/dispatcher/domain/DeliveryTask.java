package com.example.notification.dispatcher.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class DeliveryTask {
    private UUID taskId;
    private UUID notificationId;
    private UUID correlationId;
    private String channel;
    private String recipientTarget;
    private String title;
    private String body;
    private DeliveryTaskStatus status;
    private Instant createdAt;

    public DeliveryTask(UUID taskId, UUID notificationId, UUID correlationId, String channel, String recipientTarget, String title, String body) {
        this.taskId = taskId;
        this.notificationId = notificationId;
        this.correlationId = correlationId;
        this.channel = channel;
        this.recipientTarget = recipientTarget;
        this.title = title;
        this.body = body;
        this.status = DeliveryTaskStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public UUID getTaskId() { return taskId; }
    public UUID getNotificationId() { return notificationId; }
    public UUID getCorrelationId() { return correlationId; }
    public String getChannel() { return channel; }
    public String getRecipientTarget() { return recipientTarget; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public DeliveryTaskStatus getStatus() { return status; }
    public void setStatus(DeliveryTaskStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
