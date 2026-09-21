package com.example.notification.contract.routing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RoutingResultEvent {
    public static class RouteItem {
        private String channelCode;
        private String recipientId;
        private String recipientContact;
        private String content;
        private String checksum;

        public RouteItem() {}

        public RouteItem(String channelCode, String recipientId, String recipientContact, String content, String checksum) {
            this.channelCode = channelCode;
            this.recipientId = recipientId;
            this.recipientContact = recipientContact;
            this.content = content;
            this.checksum = checksum;
        }

        public String getChannelCode() { return channelCode; }
        public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

        public String getRecipientContact() { return recipientContact; }
        public void setRecipientContact(String recipientContact) { this.recipientContact = recipientContact; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getChecksum() { return checksum; }
        public void setChecksum(String checksum) { this.checksum = checksum; }
    }

    private UUID notificationId;
    private UUID runId;
    private UUID profileId;
    private UUID correlationId;
    private String title;
    private String content;
    private String checksum;
    private List<String> channels;
    private List<String> recipientIds;
    private List<RouteItem> routes;
    private Instant routedAt;

    public RoutingResultEvent() {}

    public RoutingResultEvent(UUID notificationId, UUID runId, UUID profileId, UUID correlationId,
                              String title, String content, String checksum, List<String> channels,
                              List<String> recipientIds, List<RouteItem> routes, Instant routedAt) {
        this.notificationId = notificationId;
        this.runId = runId;
        this.profileId = profileId;
        this.correlationId = correlationId;
        this.title = title;
        this.content = content;
        this.checksum = checksum;
        this.channels = channels;
        this.recipientIds = recipientIds;
        this.routes = routes;
        this.routedAt = routedAt != null ? routedAt : Instant.now();
    }

    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }

    public List<String> getRecipientIds() { return recipientIds; }
    public void setRecipientIds(List<String> recipientIds) { this.recipientIds = recipientIds; }

    public List<RouteItem> getRoutes() { return routes; }
    public void setRoutes(List<RouteItem> routes) { this.routes = routes; }

    public Instant getRoutedAt() { return routedAt; }
    public void setRoutedAt(Instant routedAt) { this.routedAt = routedAt; }

    // Compatibility getters
    public UUID notificationId() { return notificationId; }
    public UUID runId() { return runId; }
    public UUID profileId() { return profileId; }
    public UUID correlationId() { return correlationId; }
    public String title() { return title; }
    public String content() { return content; }
    public String checksum() { return checksum; }
    public List<String> channels() { return channels; }
    public List<String> recipientIds() { return recipientIds; }
    public Instant routedAt() { return routedAt; }
}
