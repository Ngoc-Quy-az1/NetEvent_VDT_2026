package com.example.notification.minibusiness.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileBusinessRuleMappingId implements Serializable {
    private UUID profileId;
    private UUID businessRuleId;
}
