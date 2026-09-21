package com.example.notification.contract.approval;

import java.time.Instant;
import java.util.UUID;

public class ApprovalResultEvent {
    private UUID notificationId;
    private UUID runId;
    private UUID profileId;
    private UUID approvalId;
    private UUID correlationId;
    private String status; 
    private Integer levelOrder;
    private UUID draftId;
    private String checksum;
    private String renderedContent;
    private String decidedBy;
    private String reason;
    private Instant decidedAt;

    public ApprovalResultEvent() {}

    public ApprovalResultEvent(UUID notificationId, UUID runId, UUID profileId, UUID approvalId, UUID correlationId,
                               String status, Integer levelOrder, UUID draftId, String checksum, String renderedContent,
                               String decidedBy, String reason, Instant decidedAt) {
        this.notificationId = notificationId;
        this.runId = runId;
        this.profileId = profileId;
        this.approvalId = approvalId;
        this.correlationId = correlationId;
        this.status = status;
        this.levelOrder = levelOrder;
        this.draftId = draftId;
        this.checksum = checksum;
        this.renderedContent = renderedContent;
        this.decidedBy = decidedBy;
        this.reason = reason;
        this.decidedAt = decidedAt;
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getLevelOrder() { return levelOrder; }
    public void setLevelOrder(Integer levelOrder) { this.levelOrder = levelOrder; }

    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public String getRenderedContent() { return renderedContent; }
    public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    // Compatibility getters
    public UUID notificationId() { return notificationId; }
    public UUID runId() { return runId; }
    public UUID profileId() { return profileId; }
    public UUID approvalId() { return approvalId; }
    public UUID correlationId() { return correlationId; }
    public String status() { return status; }
    public String checksum() { return checksum; }
    public String renderedContent() { return renderedContent; }
    public String decidedBy() { return decidedBy; }
    public String reason() { return reason; }
    public Instant decidedAt() { return decidedAt; }
}
