package com.example.notification.processor.domain.context;

import java.util.Map;
import java.util.UUID;

public class NotificationContext {
    private UUID profileId;
    private UUID correlationId;
    private String eventType;
    private Map<String, Object> kpiData;
    private boolean requireApproval;
    private String renderedTitle;
    private String renderedContent;

    public NotificationContext(UUID profileId, UUID correlationId, String eventType, Map<String, Object> kpiData) {
        this.profileId = profileId;
        this.correlationId = correlationId;
        this.eventType = eventType;
        this.kpiData = kpiData;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getKpiData() {
        return kpiData;
    }

    public boolean isRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public String getRenderedTitle() {
        return renderedTitle;
    }

    public void setRenderedTitle(String renderedTitle) {
        this.renderedTitle = renderedTitle;
    }

    public String getRenderedContent() {
        return renderedContent;
    }

    public void setRenderedContent(String renderedContent) {
        this.renderedContent = renderedContent;
    }
}
