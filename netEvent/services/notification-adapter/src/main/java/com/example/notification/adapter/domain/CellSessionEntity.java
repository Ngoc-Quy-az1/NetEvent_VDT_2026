package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cell_session")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CellSessionEntity {

    @Id
    @Column(name = "cell_session_id")
    private UUID cellSessionId;

    @Column(name = "cell_id", nullable = false)
    private UUID cellId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", insertable = false, updatable = false)
    private CellEntity cell;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", insertable = false, updatable = false)
    private EventSessionEntity eventSession;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
