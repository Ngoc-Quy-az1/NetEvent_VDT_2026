package com.example.notification.contract.audit;

import java.time.Instant;
import java.util.UUID;

/** Immutable delivery outcome published by every channel worker. */
public class DeliveryAuditEvent {
    private UUID taskId;
    private String channelCode;
    private String recipientContact;
    private String status;
    private String provider;
    private String responseCode;
    private String responseBody;
    private Instant occurredAt;

    public DeliveryAuditEvent() { }

    public DeliveryAuditEvent(UUID taskId, String channelCode, String recipientContact, String status,
                              String provider, String responseCode, String responseBody, Instant occurredAt) {
        this.taskId = taskId; this.channelCode = channelCode; this.recipientContact = recipientContact;
        this.status = status; this.provider = provider; this.responseCode = responseCode;
        this.responseBody = responseBody; this.occurredAt = occurredAt;
    }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getRecipientContact() { return recipientContact; }
    public void setRecipientContact(String recipientContact) { this.recipientContact = recipientContact; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
