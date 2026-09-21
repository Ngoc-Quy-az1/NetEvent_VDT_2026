package com.example.notification.minibusiness.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_business_rule_mapping")
@IdClass(ProfileBusinessRuleMappingId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileBusinessRuleMappingEntity {

    @Id
    @Column(name = "profile_id")
    private UUID profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private ProfileEntity profile;

    @Id
    @Column(name = "business_rule_id")
    private UUID businessRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("businessRuleId")
    @JoinColumn(name = "business_rule_id")
    private BusinessRuleEntity businessRule;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
}
