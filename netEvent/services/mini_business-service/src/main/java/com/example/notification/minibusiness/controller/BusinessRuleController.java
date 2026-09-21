package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.BusinessRuleResponse;
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
public class BusinessRuleController {

    private final ProfileWorkflowService profileWorkflowService;

    // Lấy danh sách tất cả các Quy tắc nghiệp vụ (Business Rules)
    @GetMapping({"/business-rules", "/profile-workflow/rules"})
    public ResponseEntity<ApiResponse<List<BusinessRuleResponse>>> getAllBusinessRules(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("REST request to get business rules - page: {}, size: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllBusinessRules()));
    }

    // Lấy thông tin chi tiết một Quy tắc nghiệp vụ theo ID
    @GetMapping({"/business-rules/{ruleId}", "/profile-workflow/rules/{ruleId}"})
    public ResponseEntity<ApiResponse<BusinessRuleResponse>> getBusinessRule(@PathVariable UUID ruleId) {
        log.info("REST request to get business rule by ID: {}", ruleId);
        BusinessRuleResponse response = profileWorkflowService.getBusinessRule(ruleId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Business rule not found with ID: " + ruleId));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Tạo mới Quy tắc nghiệp vụ
    @PostMapping("/business-rules")
    public ResponseEntity<ApiResponse<BusinessRuleResponse>> createBusinessRule(@RequestBody BusinessRuleResponse request) {
        log.info("REST request to create business rule: {}", request.getBusinessRuleCode());
        BusinessRuleResponse response = profileWorkflowService.createBusinessRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Cập nhật Quy tắc nghiệp vụ theo ID
    @PatchMapping("/business-rules/{ruleId}")
    public ResponseEntity<ApiResponse<BusinessRuleResponse>> updateBusinessRule(
            @PathVariable UUID ruleId, @RequestBody BusinessRuleResponse request) {
        log.info("REST request to update business rule ID: {}", ruleId);
        BusinessRuleResponse response = profileWorkflowService.updateBusinessRule(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa Quy tắc nghiệp vụ theo ID
    @DeleteMapping("/business-rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessRule(@PathVariable UUID ruleId) {
        log.info("REST request to delete business rule ID: {}", ruleId);
        profileWorkflowService.deleteBusinessRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
