package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.ChannelResponse;
import com.example.notification.minibusiness.service.ProfileWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChannelController {

    private final ProfileWorkflowService profileWorkflowService;

    @GetMapping({"/channels", "/profile-workflow/channels"})
    public ResponseEntity<ApiResponse<List<ChannelResponse>>> getAllChannels() {
        log.info("REST request to get all channels");
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllChannels()));
    }
}
