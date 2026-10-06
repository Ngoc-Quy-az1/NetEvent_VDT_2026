package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.BatchSessionDtos.*;
import com.example.notification.minibusiness.dto.EventSessionResponse;
import com.example.notification.minibusiness.domain.entity.EventSessionEntity;
import com.example.notification.minibusiness.repository.EventSessionRepository;
import com.example.notification.minibusiness.service.EventSessionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventSessionBatchController {

    private final EventSessionBatchService eventSessionBatchService;
    private final EventSessionRepository eventSessionRepository;

    @GetMapping({"/api/v1/event-sessions", "/api/event-sessions"})
    public ResponseEntity<ApiResponse<List<EventSessionResponse>>> getEventSessions(
            @RequestParam(required = false) UUID eventId) {
        List<EventSessionEntity> sessions = eventId == null
                ? eventSessionRepository.findAll()
                : eventSessionRepository.findByEventId(eventId);
        List<EventSessionResponse> response = sessions.stream()
                .map(session -> EventSessionResponse.builder()
                        .sessionId(session.getSessionId())
                        .sessionEventCode(session.getSessionEventCode())
                        .eventId(session.getEventId())
                        .startDate(session.getStartDate())
                        .endDate(session.getEndDate())
                        .status(session.getStatus())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Preview batch event session creation (Dry-run)
     */
    @PostMapping({"/api/v1/event-sessions/batch/preview", "/api/event-sessions/batch/preview"})
    public ResponseEntity<ApiResponse<BatchSessionPreviewResponse>> previewBatch(
            @RequestBody BatchCreateEventSessionRequest request) {
        log.info("REST request to preview batch event session creation (count: {})", 
                request != null && request.getSessions() != null ? request.getSessions().size() : 0);
        BatchSessionPreviewResponse response = eventSessionBatchService.previewBatch(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Execute batch event session creation
     */
    @PostMapping({"/api/v1/event-sessions/batch", "/api/event-sessions/batch"})
    public ResponseEntity<ApiResponse<BatchCreateEventSessionResponse>> createBatch(
            @RequestBody BatchCreateEventSessionRequest request) {
        log.info("REST request to execute batch event session creation (count: {})", 
                request != null && request.getSessions() != null ? request.getSessions().size() : 0);
        BatchCreateEventSessionResponse response = eventSessionBatchService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * Get all event types metadata
     */
    @GetMapping({"/api/v1/metadata/event-types", "/api/metadata/event-types"})
    public ResponseEntity<ApiResponse<List<EventTypeResponse>>> getAllEventTypes() {
        List<EventTypeResponse> list = eventSessionBatchService.getAllEventTypes();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * Get all holiday occasions metadata
     */
    @GetMapping({"/api/v1/metadata/holidays", "/api/metadata/holidays"})
    public ResponseEntity<ApiResponse<List<HolidayOccasionResponse>>> getAllHolidays() {
        List<HolidayOccasionResponse> list = eventSessionBatchService.getAllHolidays();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
