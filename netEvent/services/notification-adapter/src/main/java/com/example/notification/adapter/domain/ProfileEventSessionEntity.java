package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "profile_event_session")
@IdClass(ProfileEventSessionEntity.ProfileEventSessionId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEventSessionEntity {

    @Id
    @Column(name = "profile_id")
    private UUID profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private EventSessionEntity eventSession;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileEventSessionId implements Serializable {
        private UUID profileId;
        private UUID sessionId;
    }
}
