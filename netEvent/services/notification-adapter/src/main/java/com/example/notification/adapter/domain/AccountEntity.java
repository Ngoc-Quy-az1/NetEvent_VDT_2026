package com.example.notification.adapter.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "account")
@Getter 
@Setter 
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class AccountEntity {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_id", updatable = false, nullable = false) private UUID accountId;
    @Column(name = "username", nullable = false, unique = true) private String username;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(name = "email", nullable = false, unique = true) private String email;
    @Column(name = "cell_phone") private String cellPhone;
    @Column(name = "area_code") private String areaCode;
    @Column(name = "language") private String language;
    @Column(name = "role_id") private Short roleId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @OneToMany(mappedBy = "account") @Builder.Default private List<AccountChannelEntity> accountChannels = new ArrayList<>();
    @OneToMany(mappedBy = "account") @Builder.Default private List<ProfileAccountEntity> profileAccounts = new ArrayList<>();
    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); if (updatedAt == null) updatedAt = Instant.now(); }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
