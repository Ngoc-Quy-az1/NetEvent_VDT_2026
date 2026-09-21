package com.example.notification.routing.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications_task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTaskEntity {

    @Id
    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", insertable = false, updatable = false)
    private NotificationEntity notification;

    @Column(name = "channel_id")
    private UUID channelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private ChannelEntity channel;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private RecipientEntity recipient;

    @Column(name = "routing_template_id")
    private UUID routingTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_template_id", insertable = false, updatable = false)
    private RoutingTemplateEntity routingTemplate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
