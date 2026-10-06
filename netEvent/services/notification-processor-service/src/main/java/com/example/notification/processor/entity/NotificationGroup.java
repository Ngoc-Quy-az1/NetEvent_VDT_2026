package com.example.notification.processor.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "notification_group") @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationGroup {
    @Id @Column(name = "group_id", nullable = false, updatable = false) private UUID groupId;
    @Column(name = "channel_id") private UUID channelId;
    @Type(type = "jsonb") @Column(name = "group_config", columnDefinition = "jsonb", nullable = false) private String groupConfig;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void beforeInsert() { Instant now = Instant.now(); if (groupId == null) groupId = UUID.randomUUID(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; if (status == null) status = "ACTIVE"; }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
