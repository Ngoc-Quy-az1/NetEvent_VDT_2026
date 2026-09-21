package com.example.notification.routing.domain;

import java.util.List;
import java.util.UUID;

public class RoutingResult {
    private UUID notificationId;
    private UUID profileId;
    private UUID correlationId;
    private String title;
    private String content;
    private List<String> channels;
    private List<String> recipients;

    public RoutingResult(UUID notificationId, UUID profileId, UUID correlationId, String title, String content, List<String> channels, List<String> recipients) {
        this.notificationId = notificationId;
        this.profileId = profileId;
        this.correlationId = correlationId;
        this.title = title;
        this.content = content;
        this.channels = channels;
        this.recipients = recipients;
    }

    public UUID getNotificationId() { return notificationId; }
    public UUID getProfileId() { return profileId; }
    public UUID getCorrelationId() { return correlationId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getChannels() { return channels; }
    public List<String> getRecipients() { return recipients; }
}
