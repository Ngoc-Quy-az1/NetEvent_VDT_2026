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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private ChannelEntity channel;

    @Column(name = "template_id")
    private UUID templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", insertable = false, updatable = false)
    private TemplateEntity template;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileChannelId implements Serializable {
        private UUID profileId;
        private UUID channelId;
    }
}
