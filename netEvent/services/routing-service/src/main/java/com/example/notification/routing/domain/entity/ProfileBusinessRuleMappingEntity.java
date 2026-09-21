package com.example.notification.routing.domain.entity;

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

    @Id
    @Column(name = "business_rule_id")
    private UUID businessRuleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("businessRuleId")
    @JoinColumn(name = "business_rule_id")
    private BusinessRuleEntity businessRule;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
