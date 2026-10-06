package com.example.notification.adapter.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private UUID accountId;
    private String username;
    private String fullName;
    private String areaCode;
    private Short roleId;
    private String cellPhone;
    private UUID channelId;
    private String contactValue;
    private String label;
    private Boolean isActive;
    private String config;
    private UUID accountChannelId;

    /**
     * Hibernate exposes the custom jsonb projection type as {@code Object} in
     * JPQL constructor expressions, even though the entity field is a String.
     */
    public AccountDTO(UUID accountId, String username, String fullName, String areaCode,
                      Short roleId, String cellPhone, UUID channelId, String contactValue,
                      String label, Boolean isActive, Object config, UUID accountChannelId) {
        this.accountId = accountId;
        this.username = username;
        this.fullName = fullName;
        this.areaCode = areaCode;
        this.roleId = roleId;
        this.cellPhone = cellPhone;
        this.channelId = channelId;
        this.contactValue = contactValue;
        this.label = label;
        this.isActive = isActive;
        this.config = config == null ? null : config.toString();
        this.accountChannelId = accountChannelId;
    }
}
