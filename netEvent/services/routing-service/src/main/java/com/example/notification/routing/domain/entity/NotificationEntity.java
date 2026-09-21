package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;

    @Column(name = "notification_event_id")
    private UUID notificationEventId;

    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "contents")
    private String contents;

    @Column(name = "context_data", columnDefinition = "jsonb")
    private String contextDataJson;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationTaskEntity> notificationTasks = new ArrayList<>();

    public void addNotificationTask(NotificationTaskEntity task) {
        notificationTasks.add(task);
        task.setNotification(this);
    }

    public void removeNotificationTask(NotificationTaskEntity task) {
        notificationTasks.remove(task);
        task.setNotification(null);
    }
}
