package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureTemplateRequest {

    private UUID templateId;
    private UUID channelId;
    private String templateName;
    private String queueName;
    private String configJson;
    private String status;
}
