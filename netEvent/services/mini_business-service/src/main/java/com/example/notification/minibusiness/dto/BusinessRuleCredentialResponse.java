package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessRuleCredentialResponse {
    private String sourceUsername;
    private String sourcePassword;
}
