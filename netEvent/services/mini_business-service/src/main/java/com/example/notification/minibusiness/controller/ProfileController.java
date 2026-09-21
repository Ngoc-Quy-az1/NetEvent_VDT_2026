package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.*;
import com.example.notification.minibusiness.service.ProfileWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileWorkflowService profileWorkflowService;

    // Tạo mới Profile thông báo
    @PostMapping({"/profiles", "/profile-workflow/profiles"})
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        log.info("REST request to create profile: {}", request.getProfileName());
        ProfileResponse response = profileWorkflowService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Lấy danh sách tất cả các Profile 
    @GetMapping({"/profiles", "/profile-workflow/profiles"})
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> getAllProfiles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("REST request to get profiles - page: {}, size: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllProfiles()));
    }

    // Lấy thông tin chi tiết một Profile theo ID
    @GetMapping({"/profiles/{profileId}", "/profile-workflow/profiles/{profileId}"})
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable UUID profileId) {
        log.info("REST request to get profile by ID: {}", profileId);
        ProfileResponse response = profileWorkflowService.getProfile(profileId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Profile not found with ID: " + profileId));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Cập nhật thông tin Profile theo ID
    @PatchMapping({"/profiles/{profileId}", "/profile-workflow/profiles/{profileId}"})
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @PathVariable UUID profileId,
            @RequestBody UpdateProfileRequest request) {
        log.info("REST request to update profile ID: {}", profileId);
        ProfileResponse response = profileWorkflowService.updateProfile(profileId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa Profile theo ID
    @DeleteMapping({"/profiles/{profileId}", "/profile-workflow/profiles/{profileId}"})
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable UUID profileId) {
        log.info("REST request to delete profile ID: {}", profileId);
        profileWorkflowService.deleteProfile(profileId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Gán/cấu hình danh sách Quy tắc nghiệp vụ (Business Rules) cho Profile
    @PostMapping({"/profiles/{profileId}/rules", "/profile-workflow/profiles/{profileId}/rules"})
    public ResponseEntity<ApiResponse<Void>> configureProfileRules(
            @PathVariable UUID profileId,
            @Valid @RequestBody ConfigureProfileRuleRequest request) {
        log.info("REST request to configure rules for profile ID: {}", profileId);
        profileWorkflowService.configureProfileRules(profileId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Lấy danh sách Quy tắc nghiệp vụ (Business Rules) đã liên kết với Profile
    @GetMapping({"/profiles/{profileId}/rules", "/profile-workflow/profiles/{profileId}/rules"})
    public ResponseEntity<ApiResponse<List<BusinessRuleResponse>>> getRulesByProfileId(@PathVariable UUID profileId) {
        log.info("REST request to get rules for profile ID: {}", profileId);
        List<BusinessRuleResponse> rules = profileWorkflowService.getRulesByProfileId(profileId);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }
}
