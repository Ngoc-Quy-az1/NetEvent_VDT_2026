package com.example.notification.processor.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routing_template")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoutingTemplate {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "routing_template_id", nullable = false, updatable = false) private UUID routingTemplateId;
    @Column(name = "channel_id") private UUID channelId;
    @Column(name = "profile_id") private UUID profileId;
    @Column(name = "content", nullable = false) private String content;
    @Column(name = "channel_code") private String channelCode;
    @Column(name = "frequency_mode") private String frequencyMode;
    @Column(name = "version", nullable = false) private Integer version;
    @Column(name = "is_active", nullable = false) private Boolean active;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void beforeInsert() {
        Instant now = Instant.now();
        if (version == null) version = 1;
        if (active == null) active = true;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
