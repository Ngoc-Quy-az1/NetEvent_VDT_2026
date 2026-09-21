package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID profileId;
    private String profileName;
    private String displayName;
    private String displayNameUi;
    private String status;
    private String cronExpression;
    private Boolean requireApproval;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UUID> accountId;
    private List<String> accountName;
    private List<UUID> business_rule_id;
    private List<String> business_rule_name;
    private List<UUID> channel_id;
    private List<String> channel_name;
    
}
