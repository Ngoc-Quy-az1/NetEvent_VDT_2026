package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.EventResponse;
import com.example.notification.minibusiness.service.ProfileWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventController {

    private final ProfileWorkflowService profileWorkflowService;

    // Lấy danh sách tất cả các Sự kiện mạng / Lễ tết
    @GetMapping({"/events", "/profile-workflow/events"})
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("REST request to get events - page: {}, size: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllEvents()));
    }

    // Lấy thông tin chi tiết một Sự kiện theo ID
    @GetMapping({"/events/{eventId}", "/profile-workflow/events/{eventId}"})
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID eventId) {
        log.info("REST request to get event by ID: {}", eventId);
        EventResponse response = profileWorkflowService.getEvent(eventId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Event not found with ID: " + eventId));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Tạo mới Sự kiện mạng
    @PostMapping("/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@RequestBody EventResponse request) {
        log.info("REST request to create event: {}", request.getEventCode());
        EventResponse response = profileWorkflowService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Cập nhật thông tin Sự kiện theo ID
    @PatchMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable UUID eventId, @RequestBody EventResponse request) {
        log.info("REST request to update event ID: {}", eventId);
        EventResponse response = profileWorkflowService.updateEvent(eventId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa Sự kiện theo ID
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID eventId) {
        log.info("REST request to delete event ID: {}", eventId);
        profileWorkflowService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
