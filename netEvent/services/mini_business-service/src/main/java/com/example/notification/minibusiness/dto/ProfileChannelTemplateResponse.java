package com.example.notification.minibusiness.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProfileChannelTemplateResponse {
    private UUID channelId;
    private String channelName;
    private UUID templateId;
    private String templateName;
}
