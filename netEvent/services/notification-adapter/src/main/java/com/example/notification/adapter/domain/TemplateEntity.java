package com.example.notification.adapter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "template")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateEntity {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "template_id", updatable = false, nullable = false) private UUID templateId;
    @Column(name = "channel_id") private UUID channelId;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "channel_id", insertable = false, updatable = false) private ChannelEntity channel;
    @Column(name = "template_name", nullable = false) private String templateName;
    @Type(type = "jsonb")
    @Column(name = "config", columnDefinition = "jsonb") private String config;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void prePersist() { if (status == null) status = "ACTIVE"; if (createdAt == null) createdAt = Instant.now(); if (updatedAt == null) updatedAt = Instant.now(); }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
