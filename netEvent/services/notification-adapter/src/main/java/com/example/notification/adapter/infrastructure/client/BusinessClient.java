package com.example.notification.adapter.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mini-business.service.url:http://localhost:8080}")
    private String miniBusinessServiceUrl;

    public List<ProfileDto> getActiveProfiles() {
        String url = miniBusinessServiceUrl + "/api/v1/profile-workflow/profiles";
        try {
            ResponseEntity<List<ProfileDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ProfileDto>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to fetch active profiles from mini-business-service at {}", url, e);
        }
        return Collections.emptyList();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileDto {
        private UUID profileId;
        private String profileName;
        private String displayName;
        private String displayNameUi;
        private String status;
        private String cronExpression;
    }
}
