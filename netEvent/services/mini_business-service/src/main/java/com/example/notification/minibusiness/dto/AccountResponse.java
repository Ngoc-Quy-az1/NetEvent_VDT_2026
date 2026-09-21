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
public class AccountResponse {

    private UUID accountId;
    private String username;
    private String fullName;
    private String email;
    private String cellPhone;
    private String areaCode;
    private String language;
    private Short roleId;
    private Instant lastLogin;
    private Boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
