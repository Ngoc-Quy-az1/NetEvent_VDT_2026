package com.example.notification.processor.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ProfileBusinessRuleMappingId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "profile_id", nullable = false) private UUID profileId;
    @Column(name = "business_rule_id", nullable = false) private UUID businessRuleId;
}
