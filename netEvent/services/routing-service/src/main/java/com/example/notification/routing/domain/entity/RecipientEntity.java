package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipient")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "recipient_id", updatable = false, nullable = false)
    private UUID recipientId;

    @Column(name = "username")
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "language")
    private String language;

    @Column(name = "role_id")
    private Short roleId;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "recipient")
    @Builder.Default
    private List<NotificationTaskEntity> notificationTasks = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipientGroupMemberEntity> groupMembers = new ArrayList<>();

    public void addNotificationTask(NotificationTaskEntity task) {
        notificationTasks.add(task);
        task.setRecipient(this);
    }

    public void removeNotificationTask(NotificationTaskEntity task) {
        notificationTasks.remove(task);
        task.setRecipient(null);
    }

    public void addGroupMember(RecipientGroupMemberEntity groupMember) {
        groupMembers.add(groupMember);
        groupMember.setRecipient(this);
    }

    public void removeGroupMember(RecipientGroupMemberEntity groupMember) {
        groupMembers.remove(groupMember);
        groupMember.setRecipient(null);
    }
}
