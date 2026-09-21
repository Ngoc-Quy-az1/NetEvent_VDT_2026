package com.example.notification.minibusiness.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {

    @NotBlank(message = "Profile name is required")
    private String profileName;

    @NotBlank(message = "Display name is required")
    private String displayName;
    private String displayNameUi;
    private String cronExpression;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS][X]", timezone = "UTC")
    private Instant startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss][.SSS][X]", timezone = "UTC")
    private Instant endTime;
    private List<UUID> channelIds;
    private List<UUID> accountIds;
}
