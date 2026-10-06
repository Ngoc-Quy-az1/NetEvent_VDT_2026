package com.example.notification.minibusiness.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileChannelTemplateRequest {
    private UUID channelId;
    private UUID templateId;
}
