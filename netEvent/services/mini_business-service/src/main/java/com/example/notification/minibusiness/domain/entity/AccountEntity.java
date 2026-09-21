package com.example.notification.minibusiness.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "language")
    private String language;

    @Column(name = "role_id")
    private Short roleId;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountChannelEntity> accountChannels = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileAccountEntity> profileAccounts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (deleted == null) deleted = false;
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // Helper Methods
    public void addAccountChannel(AccountChannelEntity accountChannel) {
        accountChannels.add(accountChannel);
        accountChannel.setAccount(this);
    }

    public void removeAccountChannel(AccountChannelEntity accountChannel) {
        accountChannels.remove(accountChannel);
        accountChannel.setAccount(null);
    }

    public void addProfileAccount(ProfileAccountEntity profileAccount) {
        profileAccounts.add(profileAccount);
        profileAccount.setAccount(this);
    }

    public void removeProfileAccount(ProfileAccountEntity profileAccount) {
        profileAccounts.remove(profileAccount);
        profileAccount.setAccount(null);
    }
}
