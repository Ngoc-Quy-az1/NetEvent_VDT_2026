package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "profile_id", updatable = false, nullable = false)
    private UUID profileId;

    @Column(name = "profile_name", nullable = false, unique = true)
    private String profileName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "display_name_ui")
    private String displayNameUi;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileAccountEntity> profileAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileChannelEntity> profileChannels = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileEventSessionEntity> profileEventSessions = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileBusinessRuleMappingEntity> profileBusinessRules = new ArrayList<>();

    public void addProfileAccount(ProfileAccountEntity profileAccount) {
        profileAccounts.add(profileAccount);
        profileAccount.setProfile(this);
    }

    public void removeProfileAccount(ProfileAccountEntity profileAccount) {
        profileAccounts.remove(profileAccount);
        profileAccount.setProfile(null);
    }

    public void addProfileChannel(ProfileChannelEntity profileChannel) {
        profileChannels.add(profileChannel);
        profileChannel.setProfile(this);
    }

    public void removeProfileChannel(ProfileChannelEntity profileChannel) {
        profileChannels.remove(profileChannel);
        profileChannel.setProfile(null);
    }

    public void addProfileEventSession(ProfileEventSessionEntity profileEventSession) {
        profileEventSessions.add(profileEventSession);
        profileEventSession.setProfile(this);
    }

    public void removeProfileEventSession(ProfileEventSessionEntity profileEventSession) {
        profileEventSessions.remove(profileEventSession);
        profileEventSession.setProfile(null);
    }
}
