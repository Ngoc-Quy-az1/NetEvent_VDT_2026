package com.example.notification.minibusiness.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile")
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

    @Column(name = "created_at", nullable = false, updatable = false)
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
    private List<ProfileBusinessRuleMappingEntity> profileBusinessRules = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (status == null) status = "ACTIVE";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String profileName) { this.profileName = profileName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDisplayNameUi() { return displayNameUi; }
    public void setDisplayNameUi(String displayNameUi) { this.displayNameUi = displayNameUi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ProfileAccountEntity> getProfileAccounts() { return profileAccounts; }
    public void setProfileAccounts(List<ProfileAccountEntity> profileAccounts) { this.profileAccounts = profileAccounts; }

    public List<ProfileChannelEntity> getProfileChannels() { return profileChannels; }
    public void setProfileChannels(List<ProfileChannelEntity> profileChannels) { this.profileChannels = profileChannels; }

    public List<ProfileBusinessRuleMappingEntity> getProfileBusinessRules() { return profileBusinessRules; }
    public void setProfileBusinessRules(List<ProfileBusinessRuleMappingEntity> profileBusinessRules) { this.profileBusinessRules = profileBusinessRules; }

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

    public void addProfileBusinessRule(ProfileBusinessRuleMappingEntity mapping) {
        profileBusinessRules.add(mapping);
        mapping.setProfile(this);
    }

    public void removeProfileBusinessRule(ProfileBusinessRuleMappingEntity mapping) {
        profileBusinessRules.remove(mapping);
        mapping.setProfile(null);
    }
}
