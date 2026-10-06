package com.example.notification.minibusiness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UpdateProfileRequest {

    private String profileName;
    private String displayName;
    private String displayNameUi;
    private String status;
    private String cronExpression;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS][X]", timezone = "UTC")
    private Instant startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS][X]", timezone = "UTC")
    private Instant endTime;
    private List<UUID> channelIds;
    private List<ProfileChannelTemplateRequest> channelTemplates;
    private List<String> channels;
    private List<UUID> accountIds;
    private List<UUID> sessionIds;
    private List<ProfileGroupRequest> profileGroups;
}
