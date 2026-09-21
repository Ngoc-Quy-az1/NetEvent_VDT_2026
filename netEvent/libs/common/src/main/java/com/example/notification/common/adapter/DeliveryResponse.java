package com.example.notification.common.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {
    private boolean success;
    private String responseCode;
    private String responseMessage;
    private String externalMessageId;
}
