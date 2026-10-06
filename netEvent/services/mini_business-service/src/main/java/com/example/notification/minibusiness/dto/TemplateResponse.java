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
public class TemplateResponse {

    private UUID templateId;
    private UUID channelId;
    private String channelName;
    private String templateName;
    private String config;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
