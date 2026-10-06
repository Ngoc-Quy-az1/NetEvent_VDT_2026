package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountChannelDto {
    private UUID accountChannelId;
    private UUID accountId;
    private String accountUsername;
    private String accountFullName;
    private UUID channelId;
    private String channelName;
    private String contactValue;
    private String label;
    private Boolean isActive;
    private String config;
    private Instant createdAt;
    private Instant updatedAt;
}
