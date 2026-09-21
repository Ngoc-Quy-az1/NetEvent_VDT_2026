package com.example.notification.adapter.application.controller;

import com.example.notification.adapter.application.dto.request.ProfileTriggerRequest;
import com.example.notification.adapter.application.dto.request.ReportCreatedEvent;
import com.example.notification.adapter.application.mapper.EventIngestionMapper;
import com.example.notification.adapter.application.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IngestionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IngestionService ingestionService;

    @Spy
    private EventIngestionMapper eventIngestionMapper = Mappers.getMapper(EventIngestionMapper.class);

    @InjectMocks
    private IngestionController ingestionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ingestionController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should successfully ingest profile event and return 202 ACCEPTED with ApiResponse envelope")
    void shouldIngestProfileSuccessfully() throws Exception {
        // Given
        ProfileTriggerRequest request = ProfileTriggerRequest.builder()
                .eventCode("KPI_BREACH_001")
                .eventName("CPU High Alert")
                .profileName("INFRA_ALERT_PROFILE")
                .type("METRIC_ALERT")
                .severity("HIGH")
                .payload(Collections.singletonMap("cpuUsage", 95.5))
                .build();

        UUID generatedEventId = UUID.randomUUID();
        ReportCreatedEvent mockEvent = ReportCreatedEvent.builder()
                .eventId(generatedEventId)
                .eventCode("KPI_BREACH_001")
                .eventName("CPU High Alert")
                .type("METRIC_ALERT")
                .severity("HIGH")
                .timestamp(Instant.now())
                .build();

        when(ingestionService.processKpiReport(any(ProfileTriggerRequest.class))).thenReturn(mockEvent);

        // When & Then
        mockMvc.perform(post("/api/v1/events/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Profile event accepted for processing"))
                .andExpect(jsonPath("$.data.eventId").value(generatedEventId.toString()))
                .andExpect(jsonPath("$.data.eventCode").value("KPI_BREACH_001"))
                .andExpect(jsonPath("$.data.eventName").value("CPU High Alert"))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }
}
