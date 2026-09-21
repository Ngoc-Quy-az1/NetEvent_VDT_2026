package com.example.notification.audit.application.controller;

import com.example.notification.audit.domain.AuditHistoryEntity;
import com.example.notification.audit.infrastructure.AuditHistoryRepository;
import com.example.notification.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuditController {

    private final AuditHistoryRepository auditHistoryRepository;

    // --- Audit Queries ---
    @GetMapping("/audit/tasks/{taskId}")
    public ResponseEntity<ApiResponse<List<AuditHistoryEntity>>> getAuditByTaskId(@PathVariable UUID taskId) {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryRepository.findByTaskId(taskId)));
    }

    @GetMapping("/audit/channels/{channelCode}")
    public ResponseEntity<ApiResponse<List<AuditHistoryEntity>>> getAuditByChannelCode(@PathVariable String channelCode) {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryRepository.findByChannelCode(channelCode)));
    }

    @GetMapping("/audit/logs")
    public ResponseEntity<ApiResponse<List<AuditHistoryEntity>>> getAllAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditHistoryRepository.findAll()));
    }

    // --- Configuration REST APIs (Section 4.8) ---
    @GetMapping("/profiles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProfiles() {
        List<Map<String, Object>> profiles = List.of(
            Map.of("profileId", 1001, "profileName", "coverage_alert", "displayName", "Cảnh báo Vùng phủ sóng", "status", "ACTIVE", "requireApproval", true, "allowMute", true, "cronExpression", "0 0 * * * *"),
            Map.of("profileId", 1002, "profileName", "overshoot_alert", "displayName", "Cảnh báo Cell Overshoot", "status", "ACTIVE", "requireApproval", false, "allowMute", true, "cronExpression", "0 */15 * * * *"),
            Map.of("profileId", 1003, "profileName", "azimuth_mismatch", "displayName", "Cảnh báo Sai Azimuth", "status", "ACTIVE", "requireApproval", true, "allowMute", false, "cronExpression", "0 0 8 * * *")
        );
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @GetMapping("/business-rules")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBusinessRules() {
        List<Map<String, Object>> rules = List.of(
            Map.of("businessRuleCode", "BR_OVERSHOOT", "businessRuleName", "Cell Overshoot KPI", "sourceTable", "cell_overshoot", "status", "ACTIVE"),
            Map.of("businessRuleCode", "BR_AZIMUTH", "businessRuleName", "Cell Azimuth Mismatch KPI", "sourceTable", "cell_azimuth_mismatch_and_swap_feeder", "status", "ACTIVE"),
            Map.of("businessRuleCode", "BR_TWINBEAM", "businessRuleName", "Twinbeam Swap KPI", "sourceTable", "cell_twinbeam_swap", "status", "ACTIVE")
        );
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/channels")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChannels() {
        List<Map<String, Object>> channels = List.of(
            Map.of("channelCode", "EMAIL", "channelName", "Kênh Email SMTP", "status", "ACTIVE", "queueName", "delivery.email.v1"),
            Map.of("channelCode", "SMS", "channelName", "Kênh SMS Viettel Gateway", "status", "ACTIVE", "queueName", "delivery.sms.v1"),
            Map.of("channelCode", "OTT", "channelName", "Kênh OTT / Telegram Bot", "status", "ACTIVE", "queueName", "delivery.ott.v1")
        );
        return ResponseEntity.ok(ApiResponse.success(channels));
    }

    @GetMapping("/recipients")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecipients() {
        List<Map<String, Object>> recipients = List.of(
            Map.of("recipientId", "REC_NOC_01", "name", "NOC Staff On-Duty", "status", "ACTIVE", "channelContacts", Map.of("EMAIL", "noc-alerts@viettel.com.vn", "SMS", "+84988888888", "OTT", "@noc_telegram_bot")),
            Map.of("recipientId", "REC_MGR_02", "name", "Operations Manager", "status", "ACTIVE", "channelContacts", Map.of("EMAIL", "manager-ops@viettel.com.vn")),
            Map.of("recipientId", "REC_MUTED_03", "name", "Field Technician", "status", "MUTED", "channelContacts", Map.of("SMS", "+84977777777"))
        );
        return ResponseEntity.ok(ApiResponse.success(recipients));
    }

    @GetMapping("/routing-templates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTemplates() {
        List<Map<String, Object>> templates = List.of(
            Map.of("templateId", UUID.randomUUID().toString(), "profileId", 1001, "channelCode", "EMAIL", "version", 1, "frequencyMode", "REALTIME", "isActive", true, "content", "BÁO CÁO CẢNH BÁO CHẤT LƯỢNG MẠNG VÙNG PHỦ SỐNG {eventCode}"),
            Map.of("templateId", UUID.randomUUID().toString(), "profileId", 1001, "channelCode", "SMS", "version", 1, "frequencyMode", "REALTIME", "isActive", true, "content", "[NETEVENT] Canh bao Vung phu song event {eventCode}")
        );
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNotifications() {
        List<Map<String, Object>> notifications = List.of(
            Map.of("notificationId", UUID.randomUUID().toString(), "runId", UUID.randomUUID().toString(), "profileName", "coverage_alert", "status", "COMPLETED", "createdAt", Instant.now().minusSeconds(3600).toString()),
            Map.of("notificationId", UUID.randomUUID().toString(), "runId", UUID.randomUUID().toString(), "profileName", "overshoot_alert", "status", "WAITING_APPROVAL", "createdAt", Instant.now().minusSeconds(300).toString())
        );
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
}
