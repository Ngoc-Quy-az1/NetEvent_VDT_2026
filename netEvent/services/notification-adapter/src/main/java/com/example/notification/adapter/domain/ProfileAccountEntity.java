package com.example.notification.adapter.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "profile_account")
@IdClass(ProfileAccountEntity.ProfileAccountId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAccountEntity {

    @Id
    @Column(name = "profile_id")
    private UUID profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private AccountEntity account;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileAccountId implements Serializable {
        private UUID profileId;
        private UUID accountId;
    }
}
