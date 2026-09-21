package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "event_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSessionEntity {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "session_event_code", nullable = false, unique = true)
    private String sessionEventCode;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private EventDefinitionEntity event;

    @OneToMany(mappedBy = "eventSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CellSessionEntity> cellSessions = new ArrayList<>();

    @OneToMany(mappedBy = "eventSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileEventSessionEntity> profileEventSessions = new ArrayList<>();

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "lunar_start_date")
    private Instant lunarStartDate;

    @Column(name = "lunar_end_date")
    private Instant lunarEndDate;

    @Column(name = "expected_participants")
    private Integer expectedParticipants;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addCellSession(CellSessionEntity cellSession) {
        cellSessions.add(cellSession);
        cellSession.setEventSession(this);
    }

    public void removeCellSession(CellSessionEntity cellSession) {
        cellSessions.remove(cellSession);
        cellSession.setEventSession(null);
    }

    public void addProfileEventSession(ProfileEventSessionEntity profileEventSession) {
        profileEventSessions.add(profileEventSession);
        profileEventSession.setEventSession(this);
    }

    public void removeProfileEventSession(ProfileEventSessionEntity profileEventSession) {
        profileEventSessions.remove(profileEventSession);
        profileEventSession.setEventSession(null);
    }
}
