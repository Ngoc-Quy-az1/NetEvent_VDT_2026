package com.example.notification.contract.approval;

import java.time.Instant;
import java.util.UUID;

public class ApprovalRequestedEvent {
    private UUID notificationId;
    private UUID runId;
    private UUID profileId;
    private UUID approvalId;
    private UUID correlationId;
    private Integer currentLevel;
    private String title;
    private String renderedContent;
    private UUID templateId;
    private Integer templateVersion;
    private String checksum;
    private String approverRole;
    private Instant requestedAt;
    private Instant expiresAt;

    public ApprovalRequestedEvent() {}

    public ApprovalRequestedEvent(UUID notificationId, UUID runId, UUID profileId, UUID approvalId, UUID correlationId,
                                 Integer currentLevel, String title, String renderedContent, UUID templateId,
                                 Integer templateVersion, String checksum, String approverRole,
                                 Instant requestedAt, Instant expiresAt) {
        this.notificationId = notificationId;
        this.runId = runId;
        this.profileId = profileId;
        this.approvalId = approvalId;
        this.correlationId = correlationId;
        this.currentLevel = currentLevel;
        this.title = title;
        this.renderedContent = renderedContent;
        this.templateId = templateId;
        this.templateVersion = templateVersion;
        this.checksum = checksum;
        this.approverRole = approverRole;
        this.requestedAt = requestedAt;
        this.expiresAt = expiresAt;
    }

    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public UUID getApprovalId() { return approvalId; }
    public void setApprovalId(UUID approvalId) { this.approvalId = approvalId; }

    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRenderedContent() { return renderedContent; }
    public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public Integer getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(Integer templateVersion) { this.templateVersion = templateVersion; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    // Compatibility getters
    public UUID notificationId() { return notificationId; }
    public UUID runId() { return runId; }
    public UUID profileId() { return profileId; }
    public UUID approvalId() { return approvalId; }
    public UUID correlationId() { return correlationId; }
    public String title() { return title; }
    public String renderedContent() { return renderedContent; }
    public String contentDraft() { return renderedContent; }
    public String checksum() { return checksum; }
}
