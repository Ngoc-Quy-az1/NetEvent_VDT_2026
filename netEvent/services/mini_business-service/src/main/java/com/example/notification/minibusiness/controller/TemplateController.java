package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.ConfigureTemplateRequest;
import com.example.notification.minibusiness.dto.TemplateResponse;
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
public class TemplateController {

    private final ProfileWorkflowService profileWorkflowService;

    // Tạo / Cấu hình mẫu thông báo mới
    @PostMapping({"/templates", "/profile-workflow/templates"})
    public ResponseEntity<ApiResponse<TemplateResponse>> configureTemplate(@Valid @RequestBody ConfigureTemplateRequest request) {
        log.info("REST request to configure template: {}", request.getTemplateName());
        TemplateResponse response = profileWorkflowService.configureTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Lấy danh sách tất cả mẫu thông báo trong hệ thống
    @GetMapping({"/templates", "/profile-workflow/templates"})
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAllTemplates(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("REST request to get templates - page: {}, size: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllTemplates()));
    }

    // Lấy thông tin chi tiết một mẫu thông báo theo ID
    @GetMapping({"/templates/{templateId}", "/profile-workflow/templates/{templateId}"})
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable UUID templateId) {
        log.info("REST request to get template by ID: {}", templateId);
        TemplateResponse response = profileWorkflowService.getTemplate(templateId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Template not found with ID: " + templateId));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Cập nhật cấu hình mẫu thông báo theo ID
    @PatchMapping({"/templates/{templateId}", "/profile-workflow/templates/{templateId}"})
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable UUID templateId,
            @RequestBody ConfigureTemplateRequest request) {
        log.info("REST request to update template ID: {}", templateId);
        TemplateResponse response = profileWorkflowService.updateTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa mẫu thông báo theo ID
    @DeleteMapping({"/templates/{templateId}", "/profile-workflow/templates/{templateId}"})
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID templateId) {
        log.info("REST request to delete template ID: {}", templateId);
        profileWorkflowService.deleteTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
