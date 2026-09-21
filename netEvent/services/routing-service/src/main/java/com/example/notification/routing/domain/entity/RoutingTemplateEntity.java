package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routing_template")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingTemplateEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "routing_template_id", updatable = false, nullable = false)
    private UUID routingTemplateId;

    @Column(name = "channel_id")
    private UUID channelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private ChannelEntity channel;

    @OneToMany(mappedBy = "routingTemplate")
    @Builder.Default
    private List<NotificationTaskEntity> notificationTasks = new ArrayList<>();

    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addNotificationTask(NotificationTaskEntity task) {
        notificationTasks.add(task);
        task.setRoutingTemplate(this);
    }

    public void removeNotificationTask(NotificationTaskEntity task) {
        notificationTasks.remove(task);
        task.setRoutingTemplate(null);
    }
}
