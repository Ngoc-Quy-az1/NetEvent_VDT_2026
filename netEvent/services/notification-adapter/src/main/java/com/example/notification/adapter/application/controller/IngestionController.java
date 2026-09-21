package com.example.notification.adapter.application.controller;

import com.example.notification.adapter.application.dto.request.ProfileTriggerRequest;
import com.example.notification.adapter.application.dto.request.ReportCreatedEvent;
import com.example.notification.adapter.application.dto.response.IngestionResponse;
import com.example.notification.adapter.application.mapper.EventIngestionMapper;
import com.example.notification.adapter.application.service.IngestionService;
import com.example.notification.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;
    private final EventIngestionMapper eventIngestionMapper;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<IngestionResponse>> ingestProfile(@Valid @RequestBody ProfileTriggerRequest request) {
        log.info("Received ingestion request for profileName: {}", request.getProfileName());

        ReportCreatedEvent event = ingestionService.processKpiReport(request);
        IngestionResponse response = eventIngestionMapper.toIngestionResponse(event);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(true, "SUCCESS", "Profile event accepted for processing", response));
    }
}
