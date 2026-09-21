package com.example.notification.minibusiness.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "event_id", updatable = false, nullable = false)
    private UUID eventId;

    @Column(name = "event_code", nullable = false, unique = true)
    private String eventCode;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_type_id")
    private UUID eventTypeId;

    @Column(name = "holiday_id")
    private UUID holidayId;

    @Column(name = "event_level")
    private String eventLevel;

    @Column(name = "annual", nullable = false)
    @Builder.Default
    private Boolean annual = false;

    @Column(name = "is_lunar", nullable = false)
    @Builder.Default
    private Boolean isLunar = false;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (annual == null) annual = false;
        if (isLunar == null) isLunar = false;
        if (status == null) status = "DRAFT";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
