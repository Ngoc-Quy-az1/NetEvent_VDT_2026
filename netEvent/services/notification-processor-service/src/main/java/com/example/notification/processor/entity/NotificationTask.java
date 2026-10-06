package com.example.notification.processor.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications_task")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationTask {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "task_id", nullable = false, updatable = false) private UUID taskId;
    @Column(name = "notification_id", nullable = false) private UUID notificationId;
    @Column(name = "channel_id") private UUID channelId;
    @Column(name = "channel_code") private String channelCode;
    @Column(name = "recipient_id") private UUID recipientId;
    @Column(name = "group_id") private UUID groupId;
    @Column(name = "routing_template_id") private UUID routingTemplateId;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "retry_count", nullable = false) private Integer retryCount;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void beforeInsert() {
        Instant now = Instant.now();
        if (status == null) status = "PENDING";
        if (retryCount == null) retryCount = 0;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
