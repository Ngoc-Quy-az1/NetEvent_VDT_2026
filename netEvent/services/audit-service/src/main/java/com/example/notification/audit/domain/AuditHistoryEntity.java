package com.example.notification.audit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditHistoryEntity {

    @Id
    @Column(name = "audit_history_id")
    private UUID auditHistoryId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "channel_code", nullable = false)
    private String channelCode;

    @Column(name = "payload_snapshot", columnDefinition = "jsonb", nullable = false)
    private String payloadSnapshot;

    @Column(name = "recipient_contact", nullable = false)
    private String recipientContact;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "response_body")
    private String responseBody;
}
