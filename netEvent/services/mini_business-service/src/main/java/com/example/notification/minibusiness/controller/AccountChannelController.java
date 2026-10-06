package com.example.notification.minibusiness.controller;

import com.example.notification.common.response.ApiResponse;
import com.example.notification.minibusiness.dto.AccountChannelDto;
import com.example.notification.minibusiness.service.AccountChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AccountChannelController {

    private final AccountChannelService accountChannelService;

    // Lấy danh sách cấu hình kênh người nhận
    @GetMapping({
            "/api/v1/account-channels",
            "/api/account-channels",
            "/api/v1/profile-workflow/account-channels"
    })
    public ResponseEntity<ApiResponse<List<AccountChannelDto>>> getAccountChannels(
            @RequestParam(required = false) UUID accountId) {
        log.info("REST request to get account channels - accountId: {}", accountId);
        List<AccountChannelDto> result = accountChannelService.getAccountChannels(accountId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Lấy chi tiết một cấu hình kênh
    @GetMapping({
            "/api/v1/account-channels/{id}",
            "/api/account-channels/{id}"
    })
    public ResponseEntity<ApiResponse<AccountChannelDto>> getAccountChannel(@PathVariable UUID id) {
        log.info("REST request to get account channel by ID: {}", id);
        AccountChannelDto dto = accountChannelService.getAccountChannelById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // Tạo mới một cấu hình kênh người nhận
    @PostMapping({
            "/api/v1/account-channels",
            "/api/account-channels",
            "/api/v1/profile-workflow/account-channels"
    })
    public ResponseEntity<ApiResponse<AccountChannelDto>> createAccountChannel(@RequestBody AccountChannelDto request) {
        log.info("REST request to create account channel for account: {}", request.getAccountId());
        AccountChannelDto created = accountChannelService.createAccountChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    // Cập nhật cấu hình kênh người nhận
    @PatchMapping({
            "/api/v1/account-channels/{id}",
            "/api/account-channels/{id}"
    })
    public ResponseEntity<ApiResponse<AccountChannelDto>> updateAccountChannel(
            @PathVariable UUID id, @RequestBody AccountChannelDto request) {
        log.info("REST request to update account channel ID: {}", id);
        AccountChannelDto updated = accountChannelService.updateAccountChannel(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    // Xóa cấu hình kênh người nhận
    @DeleteMapping({
            "/api/v1/account-channels/{id}",
            "/api/account-channels/{id}"
    })
    public ResponseEntity<ApiResponse<Void>> deleteAccountChannel(@PathVariable UUID id) {
        log.info("REST request to delete account channel ID: {}", id);
        accountChannelService.deleteAccountChannel(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
