package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "notification_event_id", updatable = false, nullable = false)
    private UUID notificationEventId;

    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "raw_event_payload", columnDefinition = "jsonb", nullable = false)
    private String rawEventPayload;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = "PENDING";
        if (receivedAt == null) receivedAt = Instant.now();
        if (retryCount == null) retryCount = 0;
    }
}
