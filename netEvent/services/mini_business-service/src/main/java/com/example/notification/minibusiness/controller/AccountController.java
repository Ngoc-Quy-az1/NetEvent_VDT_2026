package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.AccountResponse;
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
public class AccountController {

    private final ProfileWorkflowService profileWorkflowService;

    // Lấy danh sách tất cả các tài khoản người dùng
    @GetMapping({"/accounts", "/profile-workflow/accounts"})
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        log.info("REST request to get accounts - page: {}, size: {}", page, size);
        return ResponseEntity.ok(ApiResponse.success(profileWorkflowService.getAllAccounts()));
    }

    // Lấy thông tin chi tiết một tài khoản theo ID
    @GetMapping({"/accounts/{accountId}", "/profile-workflow/accounts/{accountId}"})
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable UUID accountId) {
        log.info("REST request to get account by ID: {}", accountId);
        AccountResponse response = profileWorkflowService.getAccount(accountId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Account not found with ID: " + accountId));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Tạo mới tài khoản người dùng
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody AccountResponse request) {
        log.info("REST request to create account: {}", request.getUsername());
        AccountResponse response = profileWorkflowService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // Cập nhật thông tin tài khoản người dùng
    @PatchMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable UUID accountId, @RequestBody AccountResponse request) {
        log.info("REST request to update account ID: {}", accountId);
        AccountResponse response = profileWorkflowService.updateAccount(accountId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Xóa tài khoản người dùng theo ID
    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable UUID accountId) {
        log.info("REST request to delete account ID: {}", accountId);
        profileWorkflowService.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
