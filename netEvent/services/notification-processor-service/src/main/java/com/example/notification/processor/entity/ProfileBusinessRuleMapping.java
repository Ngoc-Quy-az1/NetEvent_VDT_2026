package com.example.notification.processor.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "profile_business_rule_mapping")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileBusinessRuleMapping {
    @EmbeddedId private ProfileBusinessRuleMappingId id;
    @Column(name = "status", nullable = false) private String status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void beforeInsert() {
        Instant now = Instant.now();
        if (status == null) status = "ACTIVE";
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate void beforeUpdate() { updatedAt = Instant.now(); }
}
