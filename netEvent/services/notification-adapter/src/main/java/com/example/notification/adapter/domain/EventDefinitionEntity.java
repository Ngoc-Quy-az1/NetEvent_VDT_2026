package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDefinitionEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_code", nullable = false, unique = true)
    private String eventCode;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_type_id")
    private UUID eventTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_type_id", insertable = false, updatable = false)
    private EventTypeEntity eventType;

    @Column(name = "holiday_id")
    private UUID holidayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id", insertable = false, updatable = false)
    private HolidayOccasionEntity holidayOccasion;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventSessionEntity> eventSessions = new ArrayList<>();

    @Column(name = "event_level", nullable = false)
    private String eventLevel;

    @Column(name = "annual", nullable = false)
    private Boolean annual;

    @Column(name = "is_lunar", nullable = false)
    private Boolean isLunar;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addEventSession(EventSessionEntity session) {
        eventSessions.add(session);
        session.setEvent(this);
    }

    public void removeEventSession(EventSessionEntity session) {
        eventSessions.remove(session);
        session.setEvent(null);
    }
}
