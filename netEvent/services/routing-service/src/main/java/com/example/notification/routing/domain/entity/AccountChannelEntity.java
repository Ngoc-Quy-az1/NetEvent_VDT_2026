package com.example.notification.routing.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_channel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountChannelEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_channel_id", updatable = false, nullable = false)
    private UUID accountChannelId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "channel_id", nullable = false)
    private UUID channelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private ChannelEntity channel;

    @Column(name = "contact_value", nullable = false)
    private String contactValue;

    @Column(name = "label")
    private String label;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "config", columnDefinition = "jsonb")
    private String config;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (isActive == null) isActive = true;
        if (config == null) config = "{}";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
