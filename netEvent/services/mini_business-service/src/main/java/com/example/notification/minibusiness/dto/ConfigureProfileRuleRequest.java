package com.example.notification.minibusiness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureProfileRuleRequest {

    @NotNull(message = "Rules list cannot be null")
    private List<RuleMappingItem> rules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleMappingItem {
        private UUID businessRuleId;
        private String status;
    }
}
