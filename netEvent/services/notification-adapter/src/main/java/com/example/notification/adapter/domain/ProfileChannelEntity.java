package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "profile_channel")
@IdClass(ProfileChannelEntity.ProfileChannelId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileChannelEntity {

    @Id
    @Column(name = "profile_id")
    private UUID profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Id
    @Column(name = "channel_id")
    private UUID channelId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileChannelId implements Serializable {
        private UUID profileId;
        private UUID channelId;
    }
}
