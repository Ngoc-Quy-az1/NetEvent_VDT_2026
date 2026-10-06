package com.example.notification.processor.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_attachment")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationAttachment {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "attachment_id", nullable = false, updatable = false) private UUID attachmentId;
    @Column(name = "notification_id", nullable = false) private UUID notificationId;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "storage_key", nullable = false) private String storageKey;
    @Column(name = "content_type", nullable = false) private String contentType;
    @Column(name = "size_bytes", nullable = false) private Long sizeBytes;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "deleted_at") private Instant deletedAt;

    @PrePersist void beforeInsert() {
        if (status == null) status = "READY";
        if (createdAt == null) createdAt = Instant.now();
    }
}
