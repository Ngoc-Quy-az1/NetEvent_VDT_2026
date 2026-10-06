package com.example.notification.adapter.domain;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_business_rule_mapping")
@IdClass(ProfileBusinessRuleMappingEntity.ProfileBusinessRuleMappingId.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileBusinessRuleMappingEntity {
    @Id @Column(name = "profile_id", nullable = false) private UUID profileId;
    @Id @Column(name = "business_rule_id", nullable = false) private UUID businessRuleId;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("profileId") @JoinColumn(name = "profile_id") private ProfileEntity profile;
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("businessRuleId") @JoinColumn(name = "business_rule_id") private BusinessRuleEntity businessRule;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void prePersist() { if (status == null) status = "ACTIVE"; if (createdAt == null) createdAt = Instant.now(); if (updatedAt == null) updatedAt = Instant.now(); }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
    @Data @NoArgsConstructor @AllArgsConstructor public static class ProfileBusinessRuleMappingId implements Serializable { private UUID profileId; private UUID businessRuleId; }
}
