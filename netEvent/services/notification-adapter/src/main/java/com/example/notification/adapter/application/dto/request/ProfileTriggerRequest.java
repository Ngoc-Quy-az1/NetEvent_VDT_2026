package com.example.notification.adapter.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileTriggerRequest {
    @NotBlank
    private String eventCode;
    private String eventName;
    @NotBlank
    private String profileName;
    private String type;
    private String severity;
    @NotNull
    private Map<String, Object> payload;
}
