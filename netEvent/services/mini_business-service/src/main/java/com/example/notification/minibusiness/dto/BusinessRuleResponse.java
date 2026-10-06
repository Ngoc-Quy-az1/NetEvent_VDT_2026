package com.example.notification.minibusiness.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BusinessRuleResponse {

    private UUID businessRuleId;
    private String businessRuleCode;
    private String businessRuleName;
    private String sourceDbType;
    private String sourceHost;
    private Integer sourcePort;
    private String sourceDatabase;
    private String sourceSchema;
    private String sourceTable;
    private String sourceConnectionRef;
    private String sourceUsername;
    /** Plaintext password for the configured source database. Never returned by the API. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String sourcePassword;
    private String[] sqlQueries;
    private String[] summarySqlQueries;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
