package com.example.notification.processor.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "notification_id", nullable = false, updatable = false) private UUID notificationId;
    @Column(name = "notification_event_id") private UUID notificationEventId;
    @Column(name = "profile_id") private UUID profileId;
    @Type(type = "jsonb") @Column(name = "context_data", columnDefinition = "jsonb") private String contextDataJson;
    @Type(type = "jsonb") @Column(name = "raw_data", columnDefinition = "jsonb") private String rawDataJson;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void beforeInsert() { Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
}
